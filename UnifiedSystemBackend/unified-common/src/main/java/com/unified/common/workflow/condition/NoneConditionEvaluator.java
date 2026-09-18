package com.unified.common.workflow.condition;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NoneConditionEvaluator implements WorkflowConditionEvaluator {

    @Override
    public String conditionType() {
        return "NONE";
    }

    @Override
    public boolean evaluate(String conditionConfig, Map<String, Object> metrics) {
        return true;
    }
}
