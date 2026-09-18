package com.unified.logistics.workflow;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.unified.common.constant.ApprovalStatusEnum;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.logistics.entity.LogTransfer;
import com.unified.logistics.mapper.LogTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 调拨单审批结果回写
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS("logistics")
public class TransferApprovalListener {

    private final LogTransferMapper transferMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        WfInstance instance = event.getInstance();
        if (instance == null || !event.isFinished()) {
            return;
        }
        if (!WorkflowConstants.BusinessType.TRANSFER.getCode().equals(instance.getBusinessType())) {
            return;
        }
        try {
            LogTransfer update = new LogTransfer();
            update.setId(instance.getBusinessId());
            update.setApprovalStatus(event.getPhase() == WorkflowEvent.Phase.APPROVED
                    ? ApprovalStatusEnum.APPROVED.getCode()
                    : ApprovalStatusEnum.REJECTED.getCode());
            transferMapper.updateById(update);
        } catch (Exception e) {
            log.error("调拨单审批回写失败: businessId={}, err={}", instance.getBusinessId(), e.getMessage());
        }
    }
}
