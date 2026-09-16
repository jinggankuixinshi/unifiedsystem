package com.unified.logistics.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.logistics.entity.LogWarehouse;
import com.unified.logistics.mapper.LogWarehouseMapper;
import com.unified.logistics.service.LogWarehouseService;
import org.springframework.stereotype.Service;

@Service
@DS("logistics")
public class LogWarehouseServiceImpl extends ServiceImpl<LogWarehouseMapper, LogWarehouse> implements LogWarehouseService {
}
