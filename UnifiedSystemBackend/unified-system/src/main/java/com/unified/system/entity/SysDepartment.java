package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartment extends BaseEntity {

    private String deptName;
    private String deptCode;
    private Long parentId;
    private String ancestors;
    private Integer sortOrder;
    private String leader;
    private Long leaderId;
    private String phone;
    private Integer status;

    @TableField(exist = false)
    private List<SysDepartment> children;
}
