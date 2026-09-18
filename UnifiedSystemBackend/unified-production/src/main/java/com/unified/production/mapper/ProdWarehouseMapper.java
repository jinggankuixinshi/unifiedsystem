package com.unified.production.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.production.entity.ProdWarehouse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@DS("production")
@Mapper
public interface ProdWarehouseMapper extends BaseMapper<ProdWarehouse> {

    /** 原子扣减库存（SQL 见 resources/mapper/ProdWarehouseMapper.xml），返回受影响行数 */
    int deductStock(@Param("id") Long id, @Param("quantity") BigDecimal quantity);
}
