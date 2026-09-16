package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.production.entity.ProdBom;
import com.unified.production.entity.ProdBomItem;
import com.unified.production.mapper.ProdBomItemMapper;
import com.unified.production.mapper.ProdBomMapper;
import com.unified.production.service.BomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("production")
public class BomServiceImpl extends ServiceImpl<ProdBomMapper, ProdBom> implements BomService {

    private final ProdBomItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdBom createBom(ProdBom bom, List<ProdBomItem> items) {
        baseMapper.update(null, new LambdaUpdateWrapper<ProdBom>()
                .set(ProdBom::getStatus, 0)
                .eq(ProdBom::getProductId, bom.getProductId())
                .eq(ProdBom::getStatus, 1));

        save(bom);
        for (ProdBomItem item : items) {
            item.setBomId(bom.getId());
            itemMapper.insert(item);
        }
        log.info("BOM已创建: productId={}, version={}, items={}", bom.getProductId(), bom.getVersion(), items.size());
        return bom;
    }

    @Override
    public ProdBom getActiveByProduct(Long productId) {
        return getOne(new LambdaQueryWrapper<ProdBom>()
                .eq(ProdBom::getProductId, productId)
                .eq(ProdBom::getStatus, 1));
    }

    @Override
    public List<ProdBomItem> getBomItems(Long bomId) {
        return itemMapper.selectList(new LambdaQueryWrapper<ProdBomItem>()
                .eq(ProdBomItem::getBomId, bomId));
    }
}
