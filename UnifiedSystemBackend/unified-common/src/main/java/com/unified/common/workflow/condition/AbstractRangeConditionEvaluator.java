package com.unified.common.workflow.condition;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.Map;

/**
 * 区间类条件评估基类：支持 min（含）/max（不含）与 types 过滤
 */
public abstract class AbstractRangeConditionEvaluator implements WorkflowConditionEvaluator {

    private final String conditionType;
    private final String metricKey;

    protected AbstractRangeConditionEvaluator(String conditionType, String metricKey) {
        this.conditionType = conditionType;
        this.metricKey = metricKey;
    }

    @Override
    public String conditionType() {
        return conditionType;
    }

    @Override
    public boolean evaluate(String conditionConfig, Map<String, Object> metrics) {
        JSONObject cfg = parse(conditionConfig);
        if (cfg == null || !matchTypes(cfg, metrics)) {
            return false;
        }
        Double value = toDouble(metrics.get(metricKey));
        if (value == null) {
            return false;
        }
        Double min = cfg.getDouble("min");
        Double max = cfg.getDouble("max");
        return (min == null || value >= min) && (max == null || value < max);
    }

    static JSONObject parse(String config) {
        try {
            return JSONUtil.parseObj(config == null || config.isBlank() ? "{}" : config);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean matchTypes(JSONObject cfg, Map<String, Object> metrics) {
        JSONArray types = cfg.getJSONArray("types");
        if (types == null || types.isEmpty()) {
            return true;
        }
        Object metricType = metrics.get("type");
        return metricType != null && types.contains(String.valueOf(metricType));
    }

    static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
