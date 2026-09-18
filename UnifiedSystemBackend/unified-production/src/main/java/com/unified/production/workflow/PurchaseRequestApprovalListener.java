package com.unified.production.workflow;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.unified.common.constant.ApprovalStatusEnum;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.production.entity.ProdPurchaseRequest;
import com.unified.production.mapper.ProdPurchaseRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 采购申请审批结果回写
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS("production")
public class PurchaseRequestApprovalListener {

    private final ProdPurchaseRequestMapper requestMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        WfInstance instance = event.getInstance();
        if (instance == null || !event.isFinished()) {
            return;
        }
        if (!WorkflowConstants.BusinessType.PURCHASE_REQUEST.getCode().equals(instance.getBusinessType())) {
            return;
        }
        try {
            ProdPurchaseRequest update = new ProdPurchaseRequest();
            update.setId(instance.getBusinessId());
            update.setApprovalStatus(event.getPhase() == WorkflowEvent.Phase.APPROVED
                    ? ApprovalStatusEnum.APPROVED.getCode()
                    : ApprovalStatusEnum.REJECTED.getCode());
            requestMapper.updateById(update);
        } catch (Exception e) {
            log.error("采购申请审批回写失败: businessId={}, err={}", instance.getBusinessId(), e.getMessage());
        }
    }
}
