package com.unified.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysResourceDTO {

    @NotBlank(message = "资源名称不能为空")
    private String name;

    @NotBlank(message = "资源编码不能为空")
    private String code;

    @NotNull(message = "资源类型不能为空")
    private Integer type;

    private Long parentId;
    private String path;
    private String component;
    private String icon;

    private Integer status;
}
