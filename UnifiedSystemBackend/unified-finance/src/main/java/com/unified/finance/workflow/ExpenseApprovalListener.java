package com.unified.finance.workflow;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.unified.common.constant.ApprovalStatusEnum;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.finance.entity.FinExpense;
import com.unified.finance.mapper.FinExpenseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 费用报销审批结果回写
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS("finance")
public class ExpenseApprovalListener {

    private final FinExpenseMapper expenseMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        WfInstance instance = event.getInstance();
        if (instance == null || !event.isFinished()) {
            return;
        }
        if (!WorkflowConstants.BusinessType.EXPENSE.getCode().equals(instance.getBusinessType())) {
            return;
        }
        try {
            FinExpense update = new FinExpense();
            update.setId(instance.getBusinessId());
            update.setApprovalStatus(event.getPhase() == WorkflowEvent.Phase.APPROVED
                    ? ApprovalStatusEnum.APPROVED.getCode()
                    : ApprovalStatusEnum.REJECTED.getCode());
            expenseMapper.updateById(update);
        } catch (Exception e) {
            log.error("费用报销审批回写失败: businessId={}, err={}", instance.getBusinessId(), e.getMessage());
        }
    }
}
