package com.unified.system.workflow;

import com.unified.common.constant.ApprovalStatusEnum;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEvent;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.system.entity.AttLeave;
import com.unified.system.entity.AttOvertime;
import com.unified.system.mapper.AttLeaveMapper;
import com.unified.system.mapper.AttOvertimeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 请假 / 加班审批结果回写
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceApprovalListener {

    private final AttLeaveMapper leaveMapper;
    private final AttOvertimeMapper overtimeMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        WfInstance instance = event.getInstance();
        if (instance == null || !event.isFinished()) {
            return;
        }
        int status = event.getPhase() == WorkflowEvent.Phase.APPROVED
                ? ApprovalStatusEnum.APPROVED.getCode()
                : ApprovalStatusEnum.REJECTED.getCode();
        try {
            if (WorkflowConstants.BusinessType.LEAVE.getCode().equals(instance.getBusinessType())) {
                AttLeave update = new AttLeave();
                update.setId(instance.getBusinessId());
                update.setApprovalStatus(status);
                leaveMapper.updateById(update);
            } else if (WorkflowConstants.BusinessType.OVERTIME.getCode().equals(instance.getBusinessType())) {
                AttOvertime update = new AttOvertime();
                update.setId(instance.getBusinessId());
                update.setApprovalStatus(status);
                overtimeMapper.updateById(update);
            }
        } catch (Exception e) {
            log.error("考勤审批回写失败: type={}, businessId={}, err={}",
                    instance.getBusinessType(), instance.getBusinessId(), e.getMessage());
        }
    }
}
