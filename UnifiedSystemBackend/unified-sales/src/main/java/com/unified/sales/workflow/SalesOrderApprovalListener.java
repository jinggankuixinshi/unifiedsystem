package com.unified.sales.workflow;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.unified.common.constant.ApprovalStatusEnum;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.sales.entity.SalSalesOrder;
import com.unified.sales.mapper.SalSalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 销售报单审批结果回写
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS("sales")
public class SalesOrderApprovalListener {

    private final SalSalesOrderMapper orderMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        WfInstance instance = event.getInstance();
        if (instance == null || !event.isFinished()) {
            return;
        }
        if (!WorkflowConstants.BusinessType.SALES_ORDER.getCode().equals(instance.getBusinessType())) {
            return;
        }
        try {
            SalSalesOrder update = new SalSalesOrder();
            update.setId(instance.getBusinessId());
            update.setApprovalStatus(event.getPhase() == WorkflowEvent.Phase.APPROVED
                    ? ApprovalStatusEnum.APPROVED.getCode()
                    : ApprovalStatusEnum.REJECTED.getCode());
            orderMapper.updateById(update);
        } catch (Exception e) {
            log.error("销售报单审批回写失败: businessId={}, err={}", instance.getBusinessId(), e.getMessage());
        }
    }
}
