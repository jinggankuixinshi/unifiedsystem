package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_warehouse")
public class ProdWarehouse extends BaseEntity {

    private Long materialProductId;
    private Integer itemType;
    private String batchNo;
    private LocalDate productionDate;
    private BigDecimal quantity;
    private String locationCode;
}
