package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_product")
public class ProdProduct extends BaseEntity {

    private String productCode;
    private String productName;
    private String specification;
    private String unit;
    private BigDecimal basePrice;
    private Integer status;
}
