package com.unified.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("att_record")
public class AttRecord extends BaseEntity {

    private Long userId;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private String status;
    private String remark;
}
