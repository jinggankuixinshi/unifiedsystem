package com.unified.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SysRoleDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    private Integer level;
    private String description;
    private Integer status;
}
