package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum QualityCheckResultEnum {

    QUALIFIED("合格", "合格"),
    UNQUALIFIED("不合格", "不合格");

    private final String code;
    private final String label;

    QualityCheckResultEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }
}
