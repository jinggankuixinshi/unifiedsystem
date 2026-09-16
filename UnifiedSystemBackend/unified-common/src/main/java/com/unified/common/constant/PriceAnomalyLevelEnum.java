package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum PriceAnomalyLevelEnum {

    NORMAL(0, "正常"),
    MILD(1, "轻度异常"),
    MODERATE(2, "中度异常"),
    SEVERE(3, "重度异常");

    private final int level;
    private final String label;

    PriceAnomalyLevelEnum(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public static String getLabel(int level) {
        for (PriceAnomalyLevelEnum e : values()) {
            if (e.level == level) return e.label;
        }
        return "未知";
    }
}
