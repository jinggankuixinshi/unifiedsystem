package com.unified.finance.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fin_account_subject")
public class FinAccountSubject extends BaseEntity {

    private String subjectCode;
    private String subjectName;
    private String type;
    private Long parentId;
    private Integer level;
    private Integer status;
}
