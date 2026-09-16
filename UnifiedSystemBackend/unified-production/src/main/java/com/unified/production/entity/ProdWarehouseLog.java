package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_warehouse_log")
public class ProdWarehouseLog extends BaseEntity {

    private String logNo;
    private Long warehouseId;
    private Integer changeType;
    private Long businessId;
    private String businessNo;
    private BigDecimal quantity;
    private String batchNo;
    private LocalDateTime logTime;
    private BigDecimal balanceAfter;
}
