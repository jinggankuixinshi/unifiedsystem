package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_warehouse")
public class LogWarehouse extends BaseEntity {

    private Long productId;
    private String batchNo;
    private LocalDate productionDate;
    private BigDecimal quantity;
    private String locationCode;
    private LocalDate inboundDate;
}
