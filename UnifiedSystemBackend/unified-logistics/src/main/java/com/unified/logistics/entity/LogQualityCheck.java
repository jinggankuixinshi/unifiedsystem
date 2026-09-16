package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_quality_check")
public class LogQualityCheck extends BaseEntity {

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
