package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_transfer_item")
public class LogTransferItem extends BaseEntity {

    private Long transferId;
    private Long productId;
    private BigDecimal quantity;
    private String batchNo;
    private BigDecimal unitValue;
}
