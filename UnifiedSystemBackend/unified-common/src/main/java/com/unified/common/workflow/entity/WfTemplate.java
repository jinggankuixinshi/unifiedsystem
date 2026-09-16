package com.unified.common.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_template")
public class WfTemplate extends BaseEntity {

    private String templateName;
    private String businessType;
    private String description;
    private Integer status;
    private Integer version;
}
