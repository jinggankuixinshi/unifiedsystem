package com.unified.sales.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.common.util.SequenceGenerator;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEngine;
import com.unified.sales.entity.SalPriceAnomalyConfig;
import com.unified.sales.entity.SalProductPrice;
import com.unified.sales.entity.SalSalesOrder;
import com.unified.sales.entity.SalSalesOrderItem;
import com.unified.sales.mapper.SalPriceAnomalyConfigMapper;
import com.unified.sales.mapper.SalProductPriceMapper;
import com.unified.sales.mapper.SalSalesOrderItemMapper;
import com.unified.sales.mapper.SalSalesOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderService extends ServiceImpl<SalSalesOrderMapper, SalSalesOrder> {

    private final SalSalesOrderItemMapper itemMapper;
    private final SalProductPriceMapper priceMapper;
    private final SalPriceAnomalyConfigMapper anomalyConfigMapper;
    private final WorkflowEngine workflowEngine;

    @Transactional(rollbackFor = Exception.class)
    public SalSalesOrder createOrder(SalSalesOrder order, List<SalSalesOrderItem> items) {
        order.setOrderNo(SequenceGenerator.generate("SO"));
        order.setSalespersonId(UserContext.get().getUserId());

        BigDecimal total = BigDecimal.ZERO;
        int maxAnomalyLevel = 0;

        for (SalSalesOrderItem item : items) {
            BigDecimal avgPrice = getWeightedAvgPrice(item.getProductId());
            if (avgPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal deviation = BigDecimal.ONE.subtract(item.getUnitPrice().divide(avgPrice, 4, RoundingMode.HALF_UP));
                item.setPriceDeviation(deviation);
                int level = detectAnomalyLevel(deviation);
                if (level > maxAnomalyLevel) maxAnomalyLevel = level;
            }
            item.setAmount(item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            total = total.add(item.getAmount());
        }

        order.setTotalAmount(total);
        order.setPriceAnomalyLevel(maxAnomalyLevel);
        order.setApprovalStatus(0);
        save(order);

        for (SalSalesOrderItem item : items) {
            item.setOrderId(order.getId());
            itemMapper.insert(item);
        }

        workflowEngine.startWorkflow(WorkflowConstants.BusinessType.SALES_ORDER.getCode(), order.getId());
        log.info("销售报单已创建: orderNo={}, total={}, anomalyLevel={}", order.getOrderNo(), total, maxAnomalyLevel);
        return order;
    }

    private BigDecimal getWeightedAvgPrice(Long productId) {
        List<SalProductPrice> prices = priceMapper.selectList(
                new LambdaQueryWrapper<SalProductPrice>()
                        .eq(SalProductPrice::getProductId, productId)
                        .ge(SalProductPrice::getEffectiveDate, LocalDate.now().minusMonths(3))
                        .orderByDesc(SalProductPrice::getEffectiveDate));
        if (prices.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalPrice = prices.stream()
                .map(SalProductPrice::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice.divide(new BigDecimal(String.valueOf(prices.size())), 4, RoundingMode.HALF_UP);
    }

    private int detectAnomalyLevel(BigDecimal deviation) {
        List<SalPriceAnomalyConfig> configs = anomalyConfigMapper.selectList(null);
        int maxLevel = 0;
        for (SalPriceAnomalyConfig config : configs) {
            if (deviation.compareTo(config.getThreshold()) >= 0) {
                int level = switch (config.getLevel()) {
                    case "mild" -> 1;
                    case "moderate" -> 2;
                    case "severe" -> 3;
                    default -> 0;
                };
                if (level > maxLevel) maxLevel = level;
            }
        }
        return maxLevel;
    }

    public IPage<SalSalesOrder> pageOrders(int pageNum, int pageSize, Integer status, String keyword) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<SalSalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(SalSalesOrder::getApprovalStatus, status);
        wrapper.orderByDesc(SalSalesOrder::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<SalSalesOrderItem> getOrderItems(Long orderId) {
        return itemMapper.selectList(new LambdaQueryWrapper<SalSalesOrderItem>()
                .eq(SalSalesOrderItem::getOrderId, orderId));
    }
}
