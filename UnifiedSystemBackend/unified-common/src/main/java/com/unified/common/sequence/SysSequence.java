package com.unified.common.sequence;

import com.baomidou.mybatisplus.annotation.TableName;
import com.unified.common.core.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_sequence")
public class SysSequence extends BaseEntity {

    private String prefix;
    private Integer currentSeq;
    private LocalDate updateDate;
}
