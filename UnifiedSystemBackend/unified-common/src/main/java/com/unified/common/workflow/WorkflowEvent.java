package com.unified.common.workflow;

import com.unified.common.workflow.entity.WfInstance;
import com.unified.common.workflow.entity.WfNodeTemplate;
import lombok.Getter;

/**
 * 审批流程状态事件（引擎发布，业务模块监听回写单据、通知监听器推送消息）
 */
@Getter
public class WorkflowEvent {

    public enum Phase {
        STARTED, ADVANCED, DELEGATED, APPROVED, REJECTED, CANCELLED
    }

    private final Phase phase;
    private final WfInstance instance;
    private final WfNodeTemplate currentNode;
    private final Long operatorId;
    private final String comment;

    public WorkflowEvent(Phase phase, WfInstance instance, WfNodeTemplate currentNode, Long operatorId, String comment) {
        this.phase = phase;
        this.instance = instance;
        this.currentNode = currentNode;
        this.operatorId = operatorId;
        this.comment = comment;
    }

    public boolean isFinished() {
        return phase == Phase.APPROVED || phase == Phase.REJECTED || phase == Phase.CANCELLED;
    }
}
