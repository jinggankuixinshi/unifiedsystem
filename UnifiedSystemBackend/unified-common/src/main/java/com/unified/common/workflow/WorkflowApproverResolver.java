package com.unified.common.workflow;

import com.unified.common.workflow.entity.WfNodeTemplate;

import java.util.List;

/**
 * 审批人解析器（由 unified-system 模块实现，向引擎反查用户/角色/部门数据）
 */
public interface WorkflowApproverResolver {

    /** 解析节点的候选审批人用户ID列表 */
    List<Long> resolveApprovers(WfNodeTemplate node, Long applicantId);

    /** 判断指定用户能否审批该节点（实现内应包含 admin 兜底） */
    boolean canApprove(WfNodeTemplate node, Long applicantId, Long userId);

    /** 是否管理员（撤销等操作的兜底权限） */
    boolean isAdmin(Long userId);

    /** 申请人的最高角色等级（APPLICANT_LEVEL 条件用） */
    default Integer resolveApplicantLevel(Long userId) {
        return null;
    }

    /** 全部管理员用户ID（审批人为空时的兜底通知） */
    default List<Long> resolveAdminUserIds() {
        return List.of();
    }

    /** 节点审批人的最高等级（节点未配置 node_level 时，供上推判定） */
    default int resolveNodeMaxApproverLevel(WfNodeTemplate node, Long applicantId) {
        return 0;
    }
}
