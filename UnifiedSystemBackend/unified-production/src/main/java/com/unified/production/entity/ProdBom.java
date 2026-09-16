package com.unified.production.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prod_bom")
public class ProdBom extends BaseEntity {

    private Long productId;
    private String version;
    private LocalDate effectiveDate;
    private Integer status;
}
