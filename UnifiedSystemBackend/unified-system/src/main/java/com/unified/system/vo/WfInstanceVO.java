package com.unified.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WfInstanceVO {

    private Long id;
    private Long templateId;
    private String templateName;
    private String businessType;
    private Long businessId;
    private Integer currentNodeOrder;
    private String status;
    private String result;
    private LocalDateTime approvalTime;
    private LocalDateTime createTime;
}
