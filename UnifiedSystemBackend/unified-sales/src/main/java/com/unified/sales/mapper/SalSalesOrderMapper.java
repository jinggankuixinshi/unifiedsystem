package com.unified.sales.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.sales.entity.SalSalesOrder;
import org.apache.ibatis.annotations.Mapper;

@DS("sales")
@Mapper
public interface SalSalesOrderMapper extends BaseMapper<SalSalesOrder> {
}
