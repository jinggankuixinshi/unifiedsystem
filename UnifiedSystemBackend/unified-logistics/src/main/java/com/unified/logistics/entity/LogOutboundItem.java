package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_outbound_item")
public class LogOutboundItem extends BaseEntity {

    private Long outboundId;
    private Long warehouseId;
    private BigDecimal quantity;
    private String batchNo;
}
