package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_material")
public class ProdMaterial extends BaseEntity {

    private String materialCode;
    private String materialName;
    private String specification;
    private String unit;
    private BigDecimal safetyStock;
    private Integer status;
}
