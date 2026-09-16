package com.unified.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_price_anomaly_config")
public class SalPriceAnomalyConfig extends BaseEntity {

    private String level;
    private BigDecimal threshold;
    private String action;
    private String description;
}
