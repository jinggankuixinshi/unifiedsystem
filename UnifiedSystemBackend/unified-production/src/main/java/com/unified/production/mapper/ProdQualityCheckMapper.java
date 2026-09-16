package com.unified.production.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.production.entity.ProdQualityCheck;
import org.apache.ibatis.annotations.Mapper;

@DS("production")
@Mapper
public interface ProdQualityCheckMapper extends BaseMapper<ProdQualityCheck> {
}
