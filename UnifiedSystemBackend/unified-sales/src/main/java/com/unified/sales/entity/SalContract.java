package com.unified.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sal_contract")
public class SalContract extends BaseEntity {

    private String contractNo;
    private Long salesOrderId;
    private String contentJson;
    private String sealId;
    private String esignContractId;
    private String pdfPath;
    private Integer signStatus;
}
