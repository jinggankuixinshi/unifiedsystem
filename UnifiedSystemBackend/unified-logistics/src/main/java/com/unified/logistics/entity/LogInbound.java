package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_inbound")
public class LogInbound extends BaseEntity {

    private String inboundNo;
    private String inboundType;
    private Long transferId;
    private Long qualityCheckId;
    private Integer status;
}
