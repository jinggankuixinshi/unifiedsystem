package com.unified.logistics.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.logistics.entity.LogPicking;
import org.apache.ibatis.annotations.Mapper;

@DS("logistics")
@Mapper
public interface LogPickingMapper extends BaseMapper<LogPicking> {
}
