package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_purchase_request")
public class ProdPurchaseRequest extends BaseEntity {

    private String requestNo;
    private Long applicantId;
    private Long deptId;
    private BigDecimal totalAmount;
    private String reason;
    private Integer approvalStatus;
}
