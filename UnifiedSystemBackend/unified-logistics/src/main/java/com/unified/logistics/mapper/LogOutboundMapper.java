package com.unified.logistics.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.logistics.entity.LogOutbound;
import org.apache.ibatis.annotations.Mapper;

@DS("logistics")
@Mapper
public interface LogOutboundMapper extends BaseMapper<LogOutbound> {
}
