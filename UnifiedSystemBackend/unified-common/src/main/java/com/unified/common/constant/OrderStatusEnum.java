package com.unified.common.constant;

import lombok.Getter;

@Getter
public enum OrderStatusEnum {

    PENDING(0, "待处理"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String label;

    OrderStatusEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }
}
