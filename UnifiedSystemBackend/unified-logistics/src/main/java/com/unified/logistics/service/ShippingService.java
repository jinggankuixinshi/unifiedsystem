package com.unified.logistics.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.util.SequenceGenerator;
import com.unified.logistics.entity.*;
import com.unified.logistics.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 发货管理服务
 * 销售报单终审通过 → 生成发货单 → 拣货 → 打印四联销货单 → 出库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingService extends ServiceImpl<LogShippingMapper, LogShipping> {

    private final LogShippingItemMapper shippingItemMapper;
    private final LogPickingMapper pickingMapper;

    @Transactional(rollbackFor = Exception.class)
    public LogShipping createShipping(LogShipping shipping, List<LogShippingItem> items) {
        shipping.setShippingNo(SequenceGenerator.generate("SH"));
        shipping.setStatus(1);
        shipping.setPrintStatus(0);
        save(shipping);

        for (LogShippingItem item : items) {
            item.setShippingId(shipping.getId());
            shippingItemMapper.insert(item);
        }

        log.info("发货单已创建: shippingNo={}, salesOrderId={}", shipping.getShippingNo(), shipping.getSalesOrderId());
        return shipping;
    }

    public void markPrinted(Long shippingId) {
        LogShipping shipping = getById(shippingId);
        if (shipping != null) {
            shipping.setPrintStatus(1);
            updateById(shipping);
        }
    }

    public IPage<LogShipping> pageShipping(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<LogShipping> wrapper = new LambdaQueryWrapper<LogShipping>()
                .eq(status != null, LogShipping::getStatus, status)
                .orderByDesc(LogShipping::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<LogShippingItem> getItems(Long shippingId) {
        return shippingItemMapper.selectList(new LambdaQueryWrapper<LogShippingItem>()
                .eq(LogShippingItem::getShippingId, shippingId));
    }

    public LogPicking getPickingByShipping(Long shippingId) {
        return pickingMapper.selectOne(new LambdaQueryWrapper<LogPicking>()
                .eq(LogPicking::getShippingId, shippingId));
    }
}
