package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.production.entity.ProdProduct;
import com.unified.production.mapper.ProdProductMapper;
import com.unified.production.service.ProdProductService;
import org.springframework.stereotype.Service;

@Service
@DS("production")
public class ProdProductServiceImpl extends ServiceImpl<ProdProductMapper, ProdProduct> implements ProdProductService {
}
