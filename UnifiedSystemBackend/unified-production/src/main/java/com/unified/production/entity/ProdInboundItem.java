package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_inbound_item")
public class ProdInboundItem extends BaseEntity {

    private Long inboundId;
    private Long materialProductId;
    private Integer itemType;
    private BigDecimal quantity;
    private String batchNo;
    private String locationCode;
}
