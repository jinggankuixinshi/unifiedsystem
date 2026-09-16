package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_resource_dept")
public class SysResourceDept {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resourceId;
    private Long deptId;
}
