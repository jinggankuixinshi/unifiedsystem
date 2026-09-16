package com.unified.logistics.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * 物流仓库服务 — 入库/出库/FIFO批次管理
 */
@Slf4j
@Service
@DS("logistics")
@RequiredArgsConstructor
public class LogisticsWarehouseService extends ServiceImpl<LogWarehouseMapper, LogWarehouse> {

    private final LogWarehouseLogMapper warehouseLogMapper;
    private final LogInboundMapper inboundMapper;
    private final LogInboundItemMapper inboundItemMapper;
    private final LogOutboundMapper outboundMapper;
    private final LogOutboundItemMapper outboundItemMapper;
    private final LogQualityCheckMapper qualityCheckMapper;

    @Transactional(rollbackFor = Exception.class)
    public LogInbound createInbound(LogInbound inbound, List<LogInboundItem> items) {
        inbound.setInboundNo(SequenceGenerator.generate("LI"));
        inbound.setStatus(1);
        inboundMapper.insert(inbound);

        for (LogInboundItem item : items) {
            item.setInboundId(inbound.getId());
            inboundItemMapper.insert(item);

            LogWarehouse wh = new LogWarehouse();
            wh.setProductId(item.getProductId());
            wh.setBatchNo(item.getBatchNo());
            wh.setQuantity(item.getQuantity());
            wh.setLocationCode(item.getLocationCode());
            wh.setInboundDate(java.time.LocalDate.now());
            wh.setProductionDate(java.time.LocalDate.now());
            save(wh);
        }

        log.info("物流仓入库完成: inboundNo={}, items={}", inbound.getInboundNo(), items.size());
        return inbound;
    }

    @Transactional(rollbackFor = Exception.class)
    public LogOutbound createOutbound(LogOutbound outbound, List<LogOutboundItem> items) {
        outbound.setOutboundNo(SequenceGenerator.generate("LO"));
        outbound.setStatus(1);
        outboundMapper.insert(outbound);

        for (LogOutboundItem item : items) {
            List<LogWarehouse> stocks = list(new LambdaQueryWrapper<LogWarehouse>()
                    .eq(LogWarehouse::getProductId, 0L)
                    .gt(LogWarehouse::getQuantity, BigDecimal.ZERO)
                    .orderByAsc(LogWarehouse::getInboundDate));

            BigDecimal remaining = item.getQuantity();
            for (LogWarehouse stock : stocks) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                if (stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal deduct = stock.getQuantity().min(remaining);
                stock.setQuantity(stock.getQuantity().subtract(deduct));
                updateById(stock);
                remaining = remaining.subtract(deduct);
            }

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException(ErrorCode.STOCK_INSUFFICIENT.getCode(),
                        "库存不足: 缺少 " + remaining);
            }

            item.setOutboundId(outbound.getId());
            outboundItemMapper.insert(item);
        }

        log.info("物流仓出库完成: outboundNo={}, items={}", outbound.getOutboundNo(), items.size());
        return outbound;
    }

    @Transactional(rollbackFor = Exception.class)
    public LogQualityCheck createQualityCheck(LogQualityCheck check) {
        check.setCheckTime(java.time.LocalDateTime.now());
        check.setCheckerId(UserContext.get().getUserId());

        BigDecimal qualified = check.getSampleQuantity().subtract(check.getUnqualifiedQuantity());
        check.setQualifiedQuantity(qualified);
        check.setResult(check.getUnqualifiedQuantity().compareTo(BigDecimal.ZERO) > 0 ? "不合格" : "合格");
        qualityCheckMapper.insert(check);

        log.info("质检完成: type={}, result={}, qualified={}", check.getCheckType(), check.getResult(), qualified);
        return check;
    }

    public IPage<LogWarehouse> pageStock(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<LogWarehouse>().orderByDesc(LogWarehouse::getInboundDate));
    }
}
