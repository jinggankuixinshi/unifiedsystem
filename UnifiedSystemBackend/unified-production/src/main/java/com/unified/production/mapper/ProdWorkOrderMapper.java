package com.unified.production.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.production.entity.ProdWorkOrder;
import org.apache.ibatis.annotations.Mapper;

@DS("production")
@Mapper
public interface ProdWorkOrderMapper extends BaseMapper<ProdWorkOrder> {
}
