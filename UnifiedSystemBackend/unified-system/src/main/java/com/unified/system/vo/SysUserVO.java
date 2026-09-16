package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserVO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
}
