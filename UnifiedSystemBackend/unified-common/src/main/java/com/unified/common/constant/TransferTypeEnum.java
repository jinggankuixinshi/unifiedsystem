package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum TransferTypeEnum {

    NORMAL("normal", "常规调拨"),
    SAMPLE("sample", "样品"),
    SCRAP("scrap", "报废"),
    REPAIR("repair", "返修"),
    GIFT("gift", "赠送");

    private final String code;
    private final String label;

    TransferTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String getLabel(String code) {
        for (TransferTypeEnum e : values()) {
            if (e.code.equals(code)) return e.label;
        }
        return code;
    }
}
