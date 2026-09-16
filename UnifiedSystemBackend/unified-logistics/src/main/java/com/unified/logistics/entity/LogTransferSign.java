package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_transfer_sign")
public class LogTransferSign extends BaseEntity {

    private Long transferId;
    private Long signerId;
    private LocalDateTime signTime;
    private String signType;
}
