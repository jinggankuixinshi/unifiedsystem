package com.unified.common.workflow.condition;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TypeConditionEvaluator implements WorkflowConditionEvaluator {

    @Override
    public String conditionType() {
        return "TYPE";
    }

    @Override
    public boolean evaluate(String conditionConfig, Map<String, Object> metrics) {
        JSONObject cfg = AbstractRangeConditionEvaluator.parse(conditionConfig);
        if (cfg == null) {
            return false;
        }
        JSONArray types = cfg.getJSONArray("types");
        if (types == null || types.isEmpty()) {
            return false;
        }
        Object metricType = metrics.get("type");
        return metricType != null && types.contains(String.valueOf(metricType));
    }
}
