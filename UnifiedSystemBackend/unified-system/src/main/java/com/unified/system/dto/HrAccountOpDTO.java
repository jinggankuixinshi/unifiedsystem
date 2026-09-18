package com.unified.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HrAccountOpDTO {

    @NotBlank(message = "操作类型不能为空")
    private String opType;

    private Long targetUserId;

    private String username;
    private String realName;
    private Long deptId;
    private String phone;
    private String email;
}
