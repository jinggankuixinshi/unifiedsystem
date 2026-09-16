package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_purchase_request_item")
public class ProdPurchaseRequestItem extends BaseEntity {

    private Long requestId;
    private Long materialId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
}
