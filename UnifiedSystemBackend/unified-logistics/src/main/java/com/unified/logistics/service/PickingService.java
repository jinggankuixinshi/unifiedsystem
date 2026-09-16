package com.unified.logistics.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.common.util.SequenceGenerator;
import com.unified.logistics.entity.*;
import com.unified.logistics.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拣货管理服务
 * 销售报单终审通过 → 生成拣货单 → FIFO 自动分配批次
 */
@Slf4j
@Service
@DS("logistics")
@RequiredArgsConstructor
public class PickingService extends ServiceImpl<LogPickingMapper, LogPicking> {

    private final LogPickingItemMapper pickingItemMapper;
    private final LogShippingMapper shippingMapper;
    private final LogWarehouseMapper warehouseMapper;

    @Transactional(rollbackFor = Exception.class)
    public LogPicking createPicking(Long shippingId) {
        LogShipping shipping = shippingMapper.selectById(shippingId);
        if (shipping == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "发货单不存在");
        }

        LogPicking picking = new LogPicking();
        picking.setPickingNo(SequenceGenerator.generate("PK"));
        picking.setShippingId(shippingId);
        picking.setOperatorId(UserContext.get().getUserId());
        picking.setStatus(1);
        save(picking);

        log.info("拣货单已生成: pickingNo={}, shippingId={}", picking.getPickingNo(), shippingId);
        return picking;
    }

    public List<LogPickingItem> getPickingItems(Long pickingId) {
        return pickingItemMapper.selectList(new LambdaQueryWrapper<LogPickingItem>()
                .eq(LogPickingItem::getPickingId, pickingId));
    }
}
