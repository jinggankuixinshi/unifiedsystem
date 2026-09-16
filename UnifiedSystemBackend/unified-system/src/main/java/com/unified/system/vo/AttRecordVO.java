package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AttRecordVO {

    private Long id;
    private Long userId;
    private String realName;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String status;
    private LocalDateTime createTime;
}
