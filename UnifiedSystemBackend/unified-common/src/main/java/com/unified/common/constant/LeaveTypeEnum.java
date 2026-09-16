package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum LeaveTypeEnum {

    ANNUAL("annual", "年假"),
    SICK("sick", "病假"),
    PERSONAL("personal", "事假"),
    MARRIAGE("marriage", "婚假"),
    MATERNITY("maternity", "产假"),
    FUNERAL("funeral", "丧假");

    private final String code;
    private final String label;

    LeaveTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabel(String code) {
        for (LeaveTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return code;
    }
}
