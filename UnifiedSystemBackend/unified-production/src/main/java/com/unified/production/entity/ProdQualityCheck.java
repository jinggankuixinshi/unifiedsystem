package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_quality_check")
public class ProdQualityCheck extends BaseEntity {

    private Long warehouseId;
    private String checkType;
    private String aqlStandard;
    private BigDecimal sampleQuantity;
    private BigDecimal qualifiedQuantity;
    private BigDecimal unqualifiedQuantity;
    private String result;
    private String remark;
    private Long checkerId;
    private LocalDateTime checkTime;
}
