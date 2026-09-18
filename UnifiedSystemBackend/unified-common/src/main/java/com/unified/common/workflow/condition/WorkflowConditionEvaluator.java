package com.unified.common.workflow.condition;

import java.util.Map;

/**
 * 审批条件评估 SPI：新增条件维度只需在业务模块实现本接口并注册 Bean，无需修改引擎
 */
public interface WorkflowConditionEvaluator {

    String conditionType();

    boolean evaluate(String conditionConfig, Map<String, Object> metrics);
}
