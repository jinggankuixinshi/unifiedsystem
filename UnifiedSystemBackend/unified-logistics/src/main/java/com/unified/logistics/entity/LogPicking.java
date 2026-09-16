package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_picking")
public class LogPicking extends BaseEntity {

    private String pickingNo;
    private Long shippingId;
    private Long warehouseId;
    private Long operatorId;
    private Integer status;
}
