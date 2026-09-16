package com.unified.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_sales_order")
public class SalSalesOrder extends BaseEntity {

    private String orderNo;
    private Long customerId;
    private Long salespersonId;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private LocalDate deliveryDate;
    private String warrantyTerms;
    private Integer priceAnomalyLevel;
    private Integer approvalStatus;
}
