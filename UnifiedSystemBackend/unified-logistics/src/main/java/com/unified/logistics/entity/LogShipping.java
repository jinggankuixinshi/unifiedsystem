package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_shipping")
public class LogShipping extends BaseEntity {

    private String shippingNo;
    private Long salesOrderId;
    private String shippingMethod;
    private String logisticsInfo;
    private Integer printStatus;
    private Integer status;
}
