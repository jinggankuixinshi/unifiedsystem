package com.unified.common.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_node_template")
public class WfNodeTemplate extends BaseEntity {

    private Long templateId;
    private Integer nodeOrder;
    private String approverType;
    private Long approverId;
    private String conditionType;
    private String conditionConfig;
    private String nodeName;
}
