package com.unified.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinAccountSubjectDTO {

    @NotBlank(message = "科目编码不能为空")
    private String subjectCode;

    @NotBlank(message = "科目名称不能为空")
    private String subjectName;

    @NotBlank(message = "科目类型不能为空")
    private String subjectType;

    private Long parentId;
    private Integer level;
    private String description;
}
