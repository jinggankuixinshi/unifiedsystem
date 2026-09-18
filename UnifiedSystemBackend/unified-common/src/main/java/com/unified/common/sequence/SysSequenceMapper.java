package com.unified.common.sequence;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@DS("system")
public interface SysSequenceMapper extends BaseMapper<SysSequence> {

    @Select("SELECT * FROM sys_sequence WHERE prefix = #{prefix} AND update_date = #{date} AND deleted = 0 FOR UPDATE")
    SysSequence selectForUpdate(@Param("prefix") String prefix, @Param("date") LocalDate date);
}
