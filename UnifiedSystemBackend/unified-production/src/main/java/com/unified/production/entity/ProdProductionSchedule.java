package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_production_schedule")
public class ProdProductionSchedule extends BaseEntity {

    private Long planId;
    private String productionLine;
    private String team;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
}
