package com.unified.logistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("log_outbound")
public class LogOutbound extends BaseEntity {

    private String outboundNo;
    private String outboundType;
    private Long shippingId;
    private Integer status;
}
