package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_picking_item")
public class LogPickingItem extends BaseEntity {

    private Long pickingId;
    private Long productId;
    private String batchNo;
    private BigDecimal quantity;
    private String locationCode;
}
