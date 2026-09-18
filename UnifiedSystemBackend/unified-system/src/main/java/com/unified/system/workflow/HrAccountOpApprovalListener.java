package com.unified.system.workflow;

import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.system.entity.HrAccountOp;
import com.unified.system.service.HrAccountOpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 人事账号操作单审批结果：回写状态，通过后自动执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HrAccountOpApprovalListener {

    private static final String BUSINESS_TYPE = "hr_account_op";

    private final HrAccountOpService accountOpService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        WfInstance instance = event.getInstance();
        if (instance == null || !event.isFinished()) {
            return;
        }
        if (!BUSINESS_TYPE.equals(instance.getBusinessType())) {
            return;
        }
        try {
            HrAccountOp op = accountOpService.getById(instance.getBusinessId());
            if (op == null) {
                return;
            }
            op.setApprovalStatus(event.getPhase() == WorkflowEvent.Phase.APPROVED ? 1 : 2);
            accountOpService.updateById(op);
            if (event.getPhase() == WorkflowEvent.Phase.APPROVED
                    && (op.getExecuted() == null || op.getExecuted() == 0)) {
                accountOpService.execute(op);
            }
        } catch (Exception e) {
            log.error("人事账号操作单回写失败: businessId={}, err={}", instance.getBusinessId(), e.getMessage());
        }
    }
}
