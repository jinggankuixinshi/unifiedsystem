package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.production.entity.ProdMaterial;
import com.unified.production.mapper.ProdMaterialMapper;
import com.unified.production.service.ProdMaterialService;
import org.springframework.stereotype.Service;

@Service
@DS("production")
public class ProdMaterialServiceImpl extends ServiceImpl<ProdMaterialMapper, ProdMaterial> implements ProdMaterialService {
}
