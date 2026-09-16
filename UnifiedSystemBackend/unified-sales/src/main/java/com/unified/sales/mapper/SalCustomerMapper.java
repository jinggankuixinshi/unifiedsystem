package com.unified.sales.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.sales.entity.SalCustomer;
import org.apache.ibatis.annotations.Mapper;

@DS("sales")
@Mapper
public interface SalCustomerMapper extends BaseMapper<SalCustomer> {
}
