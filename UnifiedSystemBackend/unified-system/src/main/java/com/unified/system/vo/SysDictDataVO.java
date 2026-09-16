package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysDictDataVO {

    private Long id;
    private String dictType;
    private String label;
    private String value;
    private Integer sortOrder;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}
