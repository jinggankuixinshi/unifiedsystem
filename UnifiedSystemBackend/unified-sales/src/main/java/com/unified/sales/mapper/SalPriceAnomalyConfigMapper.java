package com.unified.sales.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.sales.entity.SalPriceAnomalyConfig;
import org.apache.ibatis.annotations.Mapper;

@DS("sales")
@Mapper
public interface SalPriceAnomalyConfigMapper extends BaseMapper<SalPriceAnomalyConfig> {
}
