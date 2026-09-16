package com.unified.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_product_price")
public class SalProductPrice extends BaseEntity {

    private Long productId;
    private BigDecimal price;
    private LocalDate effectiveDate;
    private String source;
}
