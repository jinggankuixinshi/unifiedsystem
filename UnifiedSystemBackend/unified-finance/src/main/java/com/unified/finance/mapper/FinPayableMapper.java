package com.unified.finance.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.finance.entity.FinPayable;
import org.apache.ibatis.annotations.Mapper;

@DS("finance")
@Mapper
public interface FinPayableMapper extends BaseMapper<FinPayable> {
}
