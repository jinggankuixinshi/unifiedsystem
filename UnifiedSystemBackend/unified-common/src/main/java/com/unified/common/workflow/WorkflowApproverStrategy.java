package com.unified.common.workflow;

import com.unified.common.workflow.entity.WfNodeTemplate;

import java.util.List;

/**
 * 审批人解析策略 SPI：新增审批人类型只需实现本接口并注册 Bean（由 unified-system 的解析器统一调度）
 */
public interface WorkflowApproverStrategy {

    String approverType();

    List<Long> resolve(WfNodeTemplate node, Long applicantId);
}
