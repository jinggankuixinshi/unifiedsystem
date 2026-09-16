package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_bom_item")
public class ProdBomItem extends BaseEntity {

    private Long bomId;
    private Long materialId;
    private BigDecimal quantity;
    private String unit;
    private String remark;
}
