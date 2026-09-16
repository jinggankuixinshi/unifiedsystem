package com.unified.production.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.production.entity.ProdQualityCheck;

public interface QualityCheckService extends IService<ProdQualityCheck> {

    ProdQualityCheck performCheck(ProdQualityCheck check);

    IPage<ProdQualityCheck> pageChecks(int pageNum, int pageSize, String checkType);
}
