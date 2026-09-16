package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_outbound")
public class ProdOutbound extends BaseEntity {

    private String outboundNo;
    private String outboundType;
    private Long businessId;
    private String businessNo;
    private Integer status;
}
