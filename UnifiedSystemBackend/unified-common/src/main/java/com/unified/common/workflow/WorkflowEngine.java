package com.unified.common.workflow;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.workflow.condition.WorkflowConditionEvaluator;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.common.workflow.entity.WfRecord;
import com.unified.common.workflow.entity.WfTemplate;
import com.unified.common.workflow.mapper.WfInstanceMapper;
import com.unified.common.workflow.mapper.WfNodeTemplateMapper;
import com.unified.common.workflow.mapper.WfRecordMapper;
import com.unified.common.workflow.mapper.WfTemplateMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 审批工作流引擎核心服务
 * 负责：按业务指标匹配分支并创建实例、线性推进审批节点、审批人权限校验、撤销/委托、事件发布
 */
@Slf4j
@Service
@DS("system")
@RequiredArgsConstructor
public class WorkflowEngine {

    private static final String PENDING = "pending";
    private static final String APPROVED = "approved";
    private static final String REJECTED = "rejected";
    private static final String CANCELLED = "cancelled";
    private static final String SKIP_ACTION = "skip";

    private final WfTemplateMapper templateMapper;
    private final WfNodeTemplateMapper nodeTemplateMapper;
    private final WfInstanceMapper instanceMapper;
    private final WfRecordMapper recordMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectProvider<WorkflowApproverResolver> approverResolverProvider;
    private final List<WorkflowConditionEvaluator> conditionEvaluators;
    private final Map<String, WorkflowConditionEvaluator> evaluatorMap = new HashMap<>();

    @PostConstruct
    void initEvaluatorMap() {
        for (WorkflowConditionEvaluator evaluator : conditionEvaluators) {
            evaluatorMap.put(evaluator.conditionType(), evaluator);
        }
    }

    /**
     * 启动审批流程（带业务指标）
     *
     * @param metrics 业务指标：amount(金额) / ratio(价格比) / type(业务类型标识)
     */
    @Transactional(rollbackFor = Exception.class)
    public WfInstance startWorkflow(String businessType, Long businessId, Long applicantId, Map<String, Object> metrics) {
        WfInstance exist = getInstanceByBusiness(businessType, businessId);
        if (exist != null && PENDING.equals(exist.getStatus())) {
            return exist;
        }
        Map<String, Object> safeMetrics = new HashMap<>();
        if (metrics != null) {
            safeMetrics.putAll(metrics);
        }
        if (!safeMetrics.containsKey("applicantLevel") && applicantId != null) {
            Integer applicantLevel = resolveApplicantLevel(applicantId);
            if (applicantLevel != null) {
                safeMetrics.put("applicantLevel", applicantLevel);
            }
        }

        WfTemplate template = getTemplateByBusinessType(businessType);
        List<WfNodeTemplate> all = getNodesByTemplate(template.getId());
        Map<Integer, List<WfNodeTemplate>> branchMap = new TreeMap<>();
        for (WfNodeTemplate node : all) {
            int branch = node.getBranchNo() == null ? 1 : node.getBranchNo();
            branchMap.computeIfAbsent(branch, k -> new ArrayList<>()).add(node);
        }

        Integer matchedBranch = null;
        for (Map.Entry<Integer, List<WfNodeTemplate>> entry : branchMap.entrySet()) {
            WfNodeTemplate head = entry.getValue().stream()
                    .min(Comparator.comparing(WfNodeTemplate::getNodeOrder))
                    .orElse(null);
            if (head != null && matchCondition(head, safeMetrics)) {
                matchedBranch = entry.getKey();
                break;
            }
        }
        if (matchedBranch == null) {
            throw new BusinessException(ErrorCode.APPROVAL_NODE_NOT_FOUND.getCode(), "未匹配到审批分支: " + businessType);
        }
        List<WfNodeTemplate> chain = branchMap.get(matchedBranch).stream()
                .sorted(Comparator.comparing(WfNodeTemplate::getNodeOrder))
                .collect(Collectors.toList());

        WfInstance instance = new WfInstance();
        instance.setTemplateId(template.getId());
        instance.setBranchNo(matchedBranch);
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setApplicantId(applicantId);
        instance.setCurrentNodeOrder(chain.get(0).getNodeOrder());
        instance.setStatus(PENDING);
        instanceMapper.insert(instance);

        int skipped = autoAdvanceSelfApproval(instance, chain);
        if (skipped > 0) {
            instanceMapper.updateById(instance);
        }
        log.info("审批流程启动: businessType={}, businessId={}, branch={}, instanceId={}, selfSkipped={}",
                businessType, businessId, matchedBranch, instance.getId(), skipped);
        if (PENDING.equals(instance.getStatus())) {
            eventPublisher.publishEvent(new WorkflowEvent(WorkflowEvent.Phase.STARTED, instance,
                    currentNodeOf(instance, chain), applicantId, null));
        } else {
            eventPublisher.publishEvent(new WorkflowEvent(WorkflowEvent.Phase.APPROVED, instance,
                    null, applicantId, "系统自动通过（申请人自审跳过）"));
        }
        return instance;
    }

    /** 兼容旧调用：无指标、无申请人 */
    public WfInstance startWorkflow(String businessType, Long businessId) {
        return startWorkflow(businessType, businessId, null, Collections.emptyMap());
    }

    /**
     * 执行审批操作（APPROVE / REJECT / PUSH_UP）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long instanceId, Long approverId, WorkflowConstants.ApprovalAction action, String comment) {
        if (action == WorkflowConstants.ApprovalAction.DELEGATE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "委托请使用委托接口");
        }
        WfInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_FOUND);
        }
        if (!PENDING.equals(instance.getStatus())) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }
        Integer currentNodeOrder = instance.getCurrentNodeOrder();
        List<WfNodeTemplate> chain = getChain(instance);
        WfNodeTemplate currentNode = chain.stream()
                .filter(n -> n.getNodeOrder().equals(currentNodeOrder))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NODE_NOT_FOUND));

        if (!canApprove(instance, currentNode, approverId)) {
            throw new BusinessException(ErrorCode.APPROVAL_NO_PERMISSION);
        }

        insertRecord(instanceId, currentNodeOrder, approverId, action, comment);

        switch (action) {
            case APPROVE -> {
                WfNodeTemplate nextNode = chain.stream()
                        .filter(n -> n.getNodeOrder() > currentNodeOrder)
                        .findFirst()
                        .orElse(null);
                if (nextNode == null) {
                    finish(instance, APPROVED);
                } else {
                    instance.setCurrentNodeOrder(nextNode.getNodeOrder());
                    clearDelegate(instance);
                }
            }
            case REJECT -> finish(instance, REJECTED);
            case PUSH_UP -> {
                int currentLevel = effectiveLevel(currentNode, instance.getApplicantId());
                WfNodeTemplate target = null;
                for (WfNodeTemplate node : chain) {
                    if (node.getNodeOrder() <= currentNodeOrder) {
                        continue;
                    }
                    if (effectiveLevel(node, instance.getApplicantId()) > currentLevel) {
                        target = node;
                        break;
                    }
                }
                if (target == null) {
                    target = chain.stream()
                            .filter(n -> n.getNodeOrder() > currentNodeOrder)
                            .reduce((a, b) -> b)
                            .orElse(null);
                }
                if (target == null) {
                    throw new BusinessException(ErrorCode.APPROVAL_NODE_NOT_FOUND.getCode(), "无上级审批节点");
                }
                instance.setCurrentNodeOrder(target.getNodeOrder());
                clearDelegate(instance);
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不支持的审批操作");
        }

        autoAdvanceSelfApproval(instance, chain);

        if (!updateInstanceConditionally(instance, currentNodeOrder)) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }
        log.info("审批操作: instanceId={}, approverId={}, action={}, status={}", instanceId, approverId, action, instance.getStatus());

        WorkflowEvent.Phase phase = phaseOf(action, instance.getStatus());
        WfNodeTemplate eventNode = PENDING.equals(instance.getStatus()) ? currentNodeOf(instance, chain) : null;
        eventPublisher.publishEvent(new WorkflowEvent(phase, instance, eventNode, approverId, comment));
        return true;
    }

    /** 委托：当前节点审批人将审批权委托给他人（推进后失效） */
    @Transactional(rollbackFor = Exception.class)
    public void delegate(Long instanceId, Long userId, Long targetUserId, String comment) {
        if (targetUserId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "被委托人不能为空");
        }
        WfInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_FOUND);
        }
        if (!PENDING.equals(instance.getStatus())) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }
        Integer currentNodeOrder = instance.getCurrentNodeOrder();
        WfNodeTemplate currentNode = getChain(instance).stream()
                .filter(n -> n.getNodeOrder().equals(currentNodeOrder))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NODE_NOT_FOUND));
        if (!canApprove(instance, currentNode, userId)) {
            throw new BusinessException(ErrorCode.APPROVAL_NO_PERMISSION);
        }
        insertRecord(instanceId, currentNodeOrder, userId, WorkflowConstants.ApprovalAction.DELEGATE,
                (comment == null ? "" : comment + " ") + "[委托给用户#" + targetUserId + "]");

        boolean updated = instanceMapper.update(null, new LambdaUpdateWrapper<WfInstance>()
                .eq(WfInstance::getId, instanceId)
                .eq(WfInstance::getStatus, PENDING)
                .eq(WfInstance::getCurrentNodeOrder, currentNodeOrder)
                .set(WfInstance::getDelegateUserId, targetUserId)
                .set(WfInstance::getDelegateNodeOrder, currentNodeOrder)) > 0;
        if (!updated) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }
        instance.setDelegateUserId(targetUserId);
        instance.setDelegateNodeOrder(currentNodeOrder);
        eventPublisher.publishEvent(new WorkflowEvent(WorkflowEvent.Phase.DELEGATED, instance, currentNode, userId, comment));
    }

    /** 撤销：申请人或管理员 */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long instanceId, Long userId) {
        WfInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_FOUND);
        }
        if (!PENDING.equals(instance.getStatus())) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }
        boolean isApplicant = userId != null && userId.equals(instance.getApplicantId());
        if (!isApplicant && !isAdmin(userId)) {
            throw new BusinessException(ErrorCode.APPROVAL_NO_PERMISSION);
        }
        Integer currentNodeOrder = instance.getCurrentNodeOrder();
        finish(instance, CANCELLED);
        if (!updateInstanceConditionally(instance, currentNodeOrder)) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }
        log.info("审批撤销: instanceId={}, operatorId={}", instanceId, userId);
        eventPublisher.publishEvent(new WorkflowEvent(WorkflowEvent.Phase.CANCELLED, instance, null, userId, null));
    }

    public WfInstance getInstanceByBusiness(String businessType, Long businessId) {
        List<WfInstance> list = instanceMapper.selectList(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getBusinessType, businessType)
                .eq(WfInstance::getBusinessId, businessId)
                .orderByDesc(WfInstance::getId));
        return list.isEmpty() ? null : list.get(0);
    }

    public List<WfRecord> getRecords(Long instanceId) {
        return recordMapper.selectList(new LambdaQueryWrapper<WfRecord>()
                .eq(WfRecord::getInstanceId, instanceId)
                .orderByAsc(WfRecord::getNodeOrder)
                .orderByAsc(WfRecord::getActionTime));
    }

    /** 我的待办：当前节点由我（或我受托）审批且流程进行中 */
    public List<WfInstance> listPendingByApprover(Long userId) {
        List<WfInstance> pendingList = instanceMapper.selectList(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getStatus, PENDING)
                .orderByDesc(WfInstance::getId));
        Map<String, List<WfNodeTemplate>> chainCache = new HashMap<>();
        return pendingList.stream().filter(i -> {
            WfNodeTemplate current = currentNodeOf(i, chainCache);
            return current != null && canApprove(i, current, userId);
        }).collect(Collectors.toList());
    }

    /** 我的已办：我操作过的实例 */
    public List<WfInstance> listDoneByApprover(Long userId) {
        List<Long> instanceIds = recordMapper.selectList(new LambdaQueryWrapper<WfRecord>()
                        .eq(WfRecord::getApproverId, userId)
                        .orderByDesc(WfRecord::getId))
                .stream().map(WfRecord::getInstanceId).distinct().collect(Collectors.toList());
        if (instanceIds.isEmpty()) {
            return Collections.emptyList();
        }
        return instanceMapper.selectBatchIds(instanceIds);
    }

    /** 我的申请 */
    public List<WfInstance> listMyApplications(Long userId) {
        return instanceMapper.selectList(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getApplicantId, userId)
                .orderByDesc(WfInstance::getId));
    }

    // ==================== 内部方法 ====================

    private WfNodeTemplate currentNodeOf(WfInstance instance, Map<String, List<WfNodeTemplate>> chainCache) {
        String key = instance.getTemplateId() + "#" + (instance.getBranchNo() == null ? 1 : instance.getBranchNo());
        List<WfNodeTemplate> chain = chainCache.computeIfAbsent(key, k -> getChain(instance));
        Integer order = instance.getCurrentNodeOrder();
        return chain.stream().filter(n -> n.getNodeOrder().equals(order)).findFirst().orElse(null);
    }

    private WfNodeTemplate currentNodeOf(WfInstance instance, List<WfNodeTemplate> chain) {
        return chain.stream().filter(n -> n.getNodeOrder().equals(instance.getCurrentNodeOrder())).findFirst().orElse(null);
    }

    private WorkflowEvent.Phase phaseOf(WorkflowConstants.ApprovalAction action, String status) {
        if (APPROVED.equals(status)) return WorkflowEvent.Phase.APPROVED;
        if (REJECTED.equals(status)) return WorkflowEvent.Phase.REJECTED;
        return WorkflowEvent.Phase.ADVANCED;
    }

    private void finish(WfInstance instance, String status) {
        instance.setStatus(status);
        instance.setResult(status);
        instance.setApprovalTime(LocalDateTime.now());
        clearDelegate(instance);
    }

    private void clearDelegate(WfInstance instance) {
        instance.setDelegateUserId(null);
        instance.setDelegateNodeOrder(null);
    }

    /** 条件更新：状态与当前节点未变时才生效（防并发重复审批） */
    private boolean updateInstanceConditionally(WfInstance instance, Integer expectedNodeOrder) {
        return instanceMapper.update(null, new LambdaUpdateWrapper<WfInstance>()
                .eq(WfInstance::getId, instance.getId())
                .eq(WfInstance::getStatus, PENDING)
                .eq(WfInstance::getCurrentNodeOrder, expectedNodeOrder)
                .set(WfInstance::getStatus, instance.getStatus())
                .set(WfInstance::getCurrentNodeOrder, instance.getCurrentNodeOrder())
                .set(WfInstance::getResult, instance.getResult())
                .set(WfInstance::getApprovalTime, instance.getApprovalTime())
                .set(WfInstance::getDelegateUserId, instance.getDelegateUserId())
                .set(WfInstance::getDelegateNodeOrder, instance.getDelegateNodeOrder())) > 0;
    }

    private boolean canApprove(WfInstance instance, WfNodeTemplate node, Long userId) {
        if (userId == null) {
            return false;
        }
        if (node.getNodeOrder().equals(instance.getDelegateNodeOrder()) && userId.equals(instance.getDelegateUserId())) {
            return true;
        }
        WorkflowApproverResolver resolver = approverResolverProvider.getIfAvailable();
        if (resolver == null) {
            log.warn("WorkflowApproverResolver 未实现，无法校验审批人");
            return false;
        }
        try {
            return resolver.canApprove(node, instance.getApplicantId(), userId);
        } catch (Exception e) {
            log.warn("审批人解析失败: templateId={}, node={}, err={}", instance.getTemplateId(), node.getId(), e.getMessage());
            return false;
        }
    }

    private boolean isAdmin(Long userId) {
        WorkflowApproverResolver resolver = approverResolverProvider.getIfAvailable();
        if (resolver == null || userId == null) {
            return false;
        }
        try {
            return resolver.isAdmin(userId);
        } catch (Exception e) {
            return false;
        }
    }

    private void insertRecord(Long instanceId, Integer nodeOrder, Long approverId,
                              WorkflowConstants.ApprovalAction action, String comment) {
        insertRecord(instanceId, nodeOrder, approverId, action.name().toLowerCase(), comment);
    }

    private void insertRecord(Long instanceId, Integer nodeOrder, Long approverId, String action, String comment) {
        WfRecord record = new WfRecord();
        record.setInstanceId(instanceId);
        record.setNodeOrder(nodeOrder);
        record.setApproverId(approverId);
        record.setApproverAction(action);
        record.setComment(comment);
        record.setActionTime(LocalDateTime.now());
        recordMapper.insert(record);
    }

    /** 条件匹配：按条件类型分发到对应评估器（SPI） */
    private boolean matchCondition(WfNodeTemplate node, Map<String, Object> metrics) {
        String conditionType = node.getConditionType() == null ? "NONE" : node.getConditionType();
        WorkflowConditionEvaluator evaluator = evaluatorMap.get(conditionType);
        if (evaluator == null) {
            log.warn("未注册的审批条件类型: type={}, nodeId={}", conditionType, node.getId());
            return false;
        }
        return evaluator.evaluate(node.getConditionConfig(), metrics);
    }

    private List<Long> resolveApprovers(WfNodeTemplate node, Long applicantId) {
        WorkflowApproverResolver resolver = approverResolverProvider.getIfAvailable();
        if (resolver == null) {
            return Collections.emptyList();
        }
        try {
            return resolver.resolveApprovers(node, applicantId);
        } catch (Exception e) {
            log.warn("审批人解析失败: nodeId={}, err={}", node.getId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private Integer resolveApplicantLevel(Long userId) {
        WorkflowApproverResolver resolver = approverResolverProvider.getIfAvailable();
        if (resolver == null) {
            return null;
        }
        try {
            return resolver.resolveApplicantLevel(userId);
        } catch (Exception e) {
            return null;
        }
    }

    private int effectiveLevel(WfNodeTemplate node, Long applicantId) {
        if (node.getNodeLevel() != null) {
            return node.getNodeLevel();
        }
        WorkflowApproverResolver resolver = approverResolverProvider.getIfAvailable();
        if (resolver == null) {
            return 0;
        }
        try {
            return resolver.resolveNodeMaxApproverLevel(node, applicantId);
        } catch (Exception e) {
            return 0;
        }
    }

    /** 申请人自审自动跳过（当前节点唯一审批人=申请人时逐节点跳过，直至可审或完成） */
    private int autoAdvanceSelfApproval(WfInstance instance, List<WfNodeTemplate> chain) {
        int skipped = 0;
        while (PENDING.equals(instance.getStatus())) {
            WfNodeTemplate current = chain.stream()
                    .filter(n -> n.getNodeOrder().equals(instance.getCurrentNodeOrder()))
                    .findFirst()
                    .orElse(null);
            if (current == null) {
                break;
            }
            List<Long> approvers = resolveApprovers(current, instance.getApplicantId());
            if (approvers.size() != 1 || !approvers.get(0).equals(instance.getApplicantId())) {
                break;
            }
            insertRecord(instance.getId(), current.getNodeOrder(), instance.getApplicantId(),
                    SKIP_ACTION, "系统自动跳过（申请人自审）");
            skipped++;
            WfNodeTemplate next = chain.stream()
                    .filter(n -> n.getNodeOrder() > current.getNodeOrder())
                    .findFirst()
                    .orElse(null);
            if (next == null) {
                finish(instance, APPROVED);
                break;
            }
            instance.setCurrentNodeOrder(next.getNodeOrder());
            clearDelegate(instance);
        }
        return skipped;
    }

    private WfTemplate getTemplateByBusinessType(String businessType) {
        WfTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<WfTemplate>()
                .eq(WfTemplate::getBusinessType, businessType)
                .eq(WfTemplate::getStatus, 1));
        if (template == null) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_FOUND.getCode(), "未配置审批模板: " + businessType);
        }
        return template;
    }

    private List<WfNodeTemplate> getNodesByTemplate(Long templateId) {
        return nodeTemplateMapper.selectList(new LambdaQueryWrapper<WfNodeTemplate>()
                .eq(WfNodeTemplate::getTemplateId, templateId)
                .orderByAsc(WfNodeTemplate::getBranchNo)
                .orderByAsc(WfNodeTemplate::getNodeOrder));
    }

    private List<WfNodeTemplate> getChain(WfInstance instance) {
        return nodeTemplateMapper.selectList(new LambdaQueryWrapper<WfNodeTemplate>()
                .eq(WfNodeTemplate::getTemplateId, instance.getTemplateId())
                .eq(WfNodeTemplate::getBranchNo, instance.getBranchNo() == null ? 1 : instance.getBranchNo())
                .orderByAsc(WfNodeTemplate::getNodeOrder));
    }
}
