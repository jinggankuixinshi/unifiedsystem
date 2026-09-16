package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_inbound")
public class ProdInbound extends BaseEntity {

    private String inboundNo;
    private String inboundType;
    private Long supplierId;
    private Long qualityCheckId;
    private Integer status;
}
