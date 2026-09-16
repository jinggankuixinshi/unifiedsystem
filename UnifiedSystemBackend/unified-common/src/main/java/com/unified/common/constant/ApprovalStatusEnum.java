package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum ApprovalStatusEnum {

    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已驳回");

    private final int code;
    private final String label;

    ApprovalStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabel(int code) {
        for (ApprovalStatusEnum e : values()) {
            if (e.code == code) return e.label;
        }
        return "未知";
    }
}
