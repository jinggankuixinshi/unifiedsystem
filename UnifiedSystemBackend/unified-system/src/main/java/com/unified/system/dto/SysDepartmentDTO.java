package com.unified.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysDepartmentDTO {

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    @NotBlank(message = "部门编码不能为空")
    private String deptCode;

    private Long parentId;

    private String leader;
    private Long leaderId;
    private String phone;
    private Integer status;
}
