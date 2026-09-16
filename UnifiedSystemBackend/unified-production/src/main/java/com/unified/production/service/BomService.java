package com.unified.production.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.production.entity.ProdBom;
import com.unified.production.entity.ProdBomItem;

import java.util.List;

public interface BomService extends IService<ProdBom> {

    ProdBom createBom(ProdBom bom, List<ProdBomItem> items);

    ProdBom getActiveByProduct(Long productId);

    List<ProdBomItem> getBomItems(Long bomId);
}
