package com.unified.common.workflow;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.common.workflow.entity.WfRecord;
import com.unified.common.workflow.entity.WfTemplate;
import com.unified.common.workflow.mapper.WfInstanceMapper;
import com.unified.common.workflow.mapper.WfNodeTemplateMapper;
import com.unified.common.workflow.mapper.WfRecordMapper;
import com.unified.common.workflow.mapper.WfTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批工作流引擎核心服务
 * 负责创建审批实例、推进审批流程、处理审批操作
 */
@Slf4j
@Service
@DS("system")
@RequiredArgsConstructor
public class WorkflowEngine {

    private final WfTemplateMapper templateMapper;
    private final WfNodeTemplateMapper nodeTemplateMapper;
    private final WfInstanceMapper instanceMapper;
    private final WfRecordMapper recordMapper;

    /**
     * 根据业务类型创建审批实例
     */
    @Transactional(rollbackFor = Exception.class)
    public WfInstance startWorkflow(String businessType, Long businessId) {
        WfTemplate template = getTemplateByBusinessType(businessType);
        List<WfNodeTemplate> nodes = getNodesByTemplate(template.getId());

        WfInstance instance = new WfInstance();
        instance.setTemplateId(template.getId());
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setCurrentNodeOrder(nodes.isEmpty() ? 0 : nodes.get(0).getNodeOrder());
        instance.setStatus(WorkflowConstants.InstanceStatus.PENDING.name().toLowerCase());
        instanceMapper.insert(instance);

        log.info("审批流程启动: businessType={}, businessId={}, instanceId={}", businessType, businessId, instance.getId());
        return instance;
    }

    /**
     * 执行审批操作
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long instanceId, Long approverId, WorkflowConstants.ApprovalAction action, String comment) {
        WfInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new BusinessException(ErrorCode.APPROVAL_NOT_FOUND);
        }
        if (!WorkflowConstants.InstanceStatus.PENDING.name().toLowerCase().equals(instance.getStatus())) {
            throw new BusinessException(ErrorCode.APPROVAL_ALREADY);
        }

        List<WfNodeTemplate> nodes = getNodesByTemplate(instance.getTemplateId());
        WfNodeTemplate currentNode = nodes.stream()
                .filter(n -> n.getNodeOrder().equals(instance.getCurrentNodeOrder()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NODE_NOT_FOUND));

        WfRecord record = new WfRecord();
        record.setInstanceId(instanceId);
        record.setNodeOrder(currentNode.getNodeOrder());
        record.setApproverId(approverId);
        record.setApproverAction(action.name().toLowerCase());
        record.setComment(comment);
        record.setActionTime(LocalDateTime.now());
        recordMapper.insert(record);

        log.info("审批操作: instanceId={}, approverId={}, action={}", instanceId, approverId, action.name());

        switch (action) {
            case APPROVE -> {
                WfNodeTemplate nextNode = nodes.stream()
                        .filter(n -> n.getNodeOrder() > currentNode.getNodeOrder())
                        .findFirst()
                        .orElse(null);
                if (nextNode == null) {
                    instance.setStatus(WorkflowConstants.InstanceStatus.APPROVED.name().toLowerCase());
                    instance.setResult("approved");
                    instance.setApprovalTime(LocalDateTime.now());
                    log.info("审批通过: instanceId={}, businessType={}, businessId={}", instanceId, instance.getBusinessType(), instance.getBusinessId());
                } else {
                    instance.setCurrentNodeOrder(nextNode.getNodeOrder());
                }
            }
            case REJECT -> {
                instance.setStatus(WorkflowConstants.InstanceStatus.REJECTED.name().toLowerCase());
                instance.setResult("rejected");
                instance.setApprovalTime(LocalDateTime.now());
                log.info("审批驳回: instanceId={}, approverId={}, comment={}", instanceId, approverId, comment);
            }
            case PUSH_UP -> {
                WfNodeTemplate bossNode = nodes.stream()
                        .filter(n -> n.getNodeOrder() > currentNode.getNodeOrder())
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(ErrorCode.APPROVAL_NODE_NOT_FOUND.getCode(), "无上级审批节点"));
                instance.setCurrentNodeOrder(bossNode.getNodeOrder());
            }
        }
        instanceMapper.updateById(instance);
        return true;
    }

    public WfInstance getInstanceByBusiness(String businessType, Long businessId) {
        return instanceMapper.selectOne(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getBusinessType, businessType)
                .eq(WfInstance::getBusinessId, businessId));
    }

    public List<WfRecord> getRecords(Long instanceId) {
        return recordMapper.selectList(new LambdaQueryWrapper<WfRecord>()
                .eq(WfRecord::getInstanceId, instanceId)
                .orderByAsc(WfRecord::getNodeOrder));
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
                .orderByAsc(WfNodeTemplate::getNodeOrder));
    }
}
