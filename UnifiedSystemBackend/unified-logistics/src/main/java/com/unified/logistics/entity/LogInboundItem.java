package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_inbound_item")
public class LogInboundItem extends BaseEntity {

    private Long inboundId;
    private Long productId;
    private BigDecimal quantity;
    private String batchNo;
    private String locationCode;
}
