package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum ReimburseTypeEnum {

    TRAVEL("travel", "差旅"),
    OFFICE("office", "办公"),
    ENTERTAINMENT("entertainment", "招待"),
    OTHER("other", "其他");

    private final String code;
    private final String label;

    ReimburseTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabel(String code) {
        for (ReimburseTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return code;
    }
}
