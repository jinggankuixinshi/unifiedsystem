package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_work_order")
public class ProdWorkOrder extends BaseEntity {

    private Long scheduleId;
    private String orderNo;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal plannedOutput;
    private BigDecimal actualOutput;
    private Integer status;
}
