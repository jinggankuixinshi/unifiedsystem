package com.unified.system.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unified.common.workflow.WorkflowApproverResolver;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.common.workflow.entity.WfTemplate;
import com.unified.common.workflow.mapper.WfTemplateMapper;
import com.unified.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 审批消息通知：待办提醒（当前节点审批人）与结果通知（申请人），落站内信并 WebSocket 推送
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowNotificationListener {

    private final SysMessageService messageService;
    private final WorkflowApproverResolver approverResolver;
    private final WfTemplateMapper templateMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        try {
            WfInstance instance = event.getInstance();
            String bizName = businessName(instance.getBusinessType());

            if (event.isFinished()) {
                notifyApplicant(event, instance, bizName);
                return;
            }
            if (event.getPhase() == WorkflowEvent.Phase.DELEGATED) {
                if (instance.getDelegateUserId() != null) {
                    messageService.sendMessage(event.getOperatorId(), instance.getDelegateUserId(),
                            "委托审批提醒：" + bizName,
                            "您被委托审批一张单据（单号ID：" + instance.getBusinessId() + "），请及时处理。",
                            instance.getBusinessType(), instance.getBusinessId());
                }
                return;
            }
            notifyCurrentApprovers(event, instance, bizName);
        } catch (Exception e) {
            log.warn("审批通知发送失败: instanceId={}, err={}",
                    event.getInstance() == null ? null : event.getInstance().getId(), e.getMessage());
        }
    }

    private void notifyCurrentApprovers(WorkflowEvent event, WfInstance instance, String bizName) {
        WfNodeTemplate node = event.getCurrentNode();
        if (node == null) {
            return;
        }
        List<Long> approvers = approverResolver.resolveApprovers(node, instance.getApplicantId());
        if (approvers.isEmpty()) {
            // 无人可审：告警并通知管理员兜底
            log.error("审批节点无可用审批人: instanceId={}, node={}, businessType={}",
                    instance.getId(), node.getNodeName(), instance.getBusinessType());
            for (Long adminId : approverResolver.resolveAdminUserIds()) {
                messageService.sendMessage(event.getOperatorId(), adminId,
                        "审批告警：节点无可用审批人",
                        bizName + "（单号ID：" + instance.getBusinessId() + "）的节点【" + node.getNodeName()
                                + "】没有解析到审批人，请检查角色/部门负责人配置或代为审批。",
                        instance.getBusinessType(), instance.getBusinessId());
            }
            return;
        }
        for (Long approverId : approvers) {
            if (approverId.equals(event.getOperatorId())) {
                continue;
            }
            messageService.sendMessage(event.getOperatorId(), approverId,
                    "待办审批：" + bizName,
                    "有一张单据等待您审批（单号ID：" + instance.getBusinessId() + "），节点：" + node.getNodeName() + "。",
                    instance.getBusinessType(), instance.getBusinessId());
        }
    }

    private void notifyApplicant(WorkflowEvent event, WfInstance instance, String bizName) {
        if (instance.getApplicantId() == null) {
            return;
        }
        String result = switch (event.getPhase()) {
            case APPROVED -> "已通过";
            case REJECTED -> "已驳回";
            case CANCELLED -> "已撤销";
            default -> "状态更新";
        };
        messageService.sendMessage(event.getOperatorId(), instance.getApplicantId(),
                "审批结果：" + bizName + result,
                "您的单据（单号ID：" + instance.getBusinessId() + "）审批" + result + "。",
                instance.getBusinessType(), instance.getBusinessId());
    }

    /** 业务名称：优先内置枚举，其次取模板名称（DB），最后回退业务类型码 —— 新增业务无需改通知代码 */
    private String businessName(String businessType) {
        for (WorkflowConstants.BusinessType type : WorkflowConstants.BusinessType.values()) {
            if (type.getCode().equals(businessType)) {
                return type.getName();
            }
        }
        WfTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<WfTemplate>()
                .eq(WfTemplate::getBusinessType, businessType));
        return template != null ? template.getTemplateName() : businessType;
    }
}
