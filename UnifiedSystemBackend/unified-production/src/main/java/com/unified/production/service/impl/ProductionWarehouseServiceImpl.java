package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.util.SequenceGenerator;
import com.unified.production.entity.*;
import com.unified.production.mapper.*;
import com.unified.production.service.ProductionWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("production")
public class ProductionWarehouseServiceImpl extends ServiceImpl<ProdWarehouseMapper, ProdWarehouse> implements ProductionWarehouseService {

    private final ProdInboundMapper inboundMapper;
    private final ProdInboundItemMapper inboundItemMapper;
    private final ProdOutboundMapper outboundMapper;
    private final ProdOutboundItemMapper outboundItemMapper;
    private final ProdWarehouseLogMapper warehouseLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdInbound createInbound(ProdInbound inbound, List<ProdInboundItem> items) {
        inbound.setInboundNo(SequenceGenerator.generate("PI"));
        inbound.setStatus(1);
        inboundMapper.insert(inbound);

        for (ProdInboundItem item : items) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "入库数量必须大于0");
            }

            item.setInboundId(inbound.getId());
            inboundItemMapper.insert(item);

            ProdWarehouse existing = getOne(new LambdaQueryWrapper<ProdWarehouse>()
                    .eq(ProdWarehouse::getMaterialProductId, item.getMaterialProductId())
                    .eq(ProdWarehouse::getItemType, item.getItemType())
                    .eq(ProdWarehouse::getBatchNo, item.getBatchNo()));
            Long whId;
            if (existing != null) {
                existing.setQuantity(existing.getQuantity().add(item.getQuantity()));
                updateById(existing);
                whId = existing.getId();
            } else {
                ProdWarehouse wh = new ProdWarehouse();
                wh.setMaterialProductId(item.getMaterialProductId());
                wh.setItemType(item.getItemType());
                wh.setBatchNo(item.getBatchNo());
                wh.setQuantity(item.getQuantity());
                wh.setLocationCode(item.getLocationCode());
                wh.setProductionDate(LocalDate.now());
                save(wh);
                whId = wh.getId();
            }

            ProdWarehouseLog logEntry = new ProdWarehouseLog();
            logEntry.setLogNo(SequenceGenerator.generate("WL"));
            logEntry.setWarehouseId(whId);
            logEntry.setChangeType(1);
            logEntry.setBusinessId(inbound.getId());
            logEntry.setBusinessNo(inbound.getInboundNo());
            logEntry.setQuantity(item.getQuantity());
            logEntry.setBatchNo(item.getBatchNo());
            logEntry.setLogTime(LocalDateTime.now());
            logEntry.setBalanceAfter(BigDecimal.ZERO);
            warehouseLogMapper.insert(logEntry);
        }

        log.info("生产仓入库完成: inboundNo={}, items={}", inbound.getInboundNo(), items.size());
        return inbound;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdOutbound createOutbound(ProdOutbound outbound, List<ProdOutboundItem> items) {
        outbound.setOutboundNo(SequenceGenerator.generate("PO"));
        outbound.setStatus(1);
        outboundMapper.insert(outbound);

        Set<Long> productIds = items.stream()
                .map(ProdOutboundItem::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, List<ProdWarehouse>> stockMap = new HashMap<>();
        if (!productIds.isEmpty()) {
            List<ProdWarehouse> allStocks = list(new LambdaQueryWrapper<ProdWarehouse>()
                    .in(ProdWarehouse::getMaterialProductId, productIds)
                    .gt(ProdWarehouse::getQuantity, BigDecimal.ZERO)
                    .orderByAsc(ProdWarehouse::getProductionDate));
            stockMap = allStocks.stream()
                    .collect(Collectors.groupingBy(ProdWarehouse::getMaterialProductId));
        }

        for (ProdOutboundItem item : items) {
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "出库数量必须大于0");
            }

            List<ProdWarehouse> stocks = stockMap.getOrDefault(item.getWarehouseId(), Collections.emptyList());
            BigDecimal remaining = item.getQuantity();

            for (ProdWarehouse stock : stocks) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal deduct = remaining.min(stock.getQuantity());
                int updated = baseMapper.update(null, new LambdaUpdateWrapper<ProdWarehouse>()
                        .setSql("quantity = quantity - " + deduct)
                        .eq(ProdWarehouse::getId, stock.getId())
                        .ge(ProdWarehouse::getQuantity, deduct));
                if (updated == 0) {
                    continue;
                }
                stock.setQuantity(stock.getQuantity().subtract(deduct));
                remaining = remaining.subtract(deduct);

                ProdWarehouseLog logEntry = new ProdWarehouseLog();
                logEntry.setLogNo(SequenceGenerator.generate("WL"));
                logEntry.setWarehouseId(stock.getId());
                logEntry.setChangeType(2);
                logEntry.setBusinessId(outbound.getId());
                logEntry.setBusinessNo(outbound.getOutboundNo());
                logEntry.setQuantity(deduct.negate());
                logEntry.setBatchNo(stock.getBatchNo());
                logEntry.setLogTime(LocalDateTime.now());
                logEntry.setBalanceAfter(stock.getQuantity());
                warehouseLogMapper.insert(logEntry);
            }

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT.getCode(),
                        "库存不足，物料ID: " + item.getWarehouseId() + ", 缺少: " + remaining);
            }

            item.setOutboundId(outbound.getId());
            outboundItemMapper.insert(item);
        }

        log.info("生产仓出库完成: outboundNo={}, items={}", outbound.getOutboundNo(), items.size());
        return outbound;
    }
}
