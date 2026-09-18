package com.unified.common.sequence;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@DS("system")
public interface SysSequenceMapper extends BaseMapper<SysSequence> {

    /** 行锁获取当日序列（SQL 见 resources/mapper/SysSequenceMapper.xml） */
    SysSequence selectForUpdate(@Param("prefix") String prefix, @Param("date") LocalDate date);
}
