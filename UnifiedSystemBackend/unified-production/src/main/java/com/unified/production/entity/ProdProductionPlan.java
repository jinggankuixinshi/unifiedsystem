package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_production_plan")
public class ProdProductionPlan extends BaseEntity {

    private String planNo;
    private Long productId;
    private BigDecimal planQuantity;
    private LocalDate planDate;
    private Integer status;
}
