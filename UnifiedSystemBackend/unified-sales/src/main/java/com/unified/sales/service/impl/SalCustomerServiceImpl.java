package com.unified.sales.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.sales.entity.SalCustomer;
import com.unified.sales.mapper.SalCustomerMapper;
import com.unified.sales.service.SalCustomerService;
import org.springframework.stereotype.Service;

@Service
@DS("sales")
public class SalCustomerServiceImpl extends ServiceImpl<SalCustomerMapper, SalCustomer> implements SalCustomerService {
}
