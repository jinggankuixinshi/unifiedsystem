package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_transfer")
public class LogTransfer extends BaseEntity {

    private String transferNo;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private String type;
    private BigDecimal totalValue;
    private String remark;
    private Integer approvalStatus;
}
