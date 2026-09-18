package com.unified.sales.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@DS("sales")
@RequiredArgsConstructor
public class SalesOrderService extends ServiceImpl<SalSalesOrderMapper, SalSalesOrder> {

    private final SalSalesOrderItemMapper itemMapper;
    private final SalProductPriceMapper priceMapper;
    private final SalPriceAnomalyConfigMapper anomalyConfigMapper;
    private final WorkflowEngine workflowEngine;

    @DSTransactional(rollbackFor = Exception.class)
    public SalSalesOrder createOrder(SalSalesOrder order, List<SalSalesOrderItem> items) {
        order.setOrderNo(SequenceGenerator.generate("SO"));
        order.setSalespersonId(UserContext.get().getUserId());

        for (SalSalesOrderItem item : items) {
            if (item.getUnitPrice() == null) item.setUnitPrice(BigDecimal.ZERO);
            if (item.getQuantity() == null) item.setQuantity(BigDecimal.ZERO);
        }

        BigDecimal total = BigDecimal.ZERO;
        int maxAnomalyLevel = 0;
        BigDecimal worstRatio = null;

        for (SalSalesOrderItem item : items) {
            BigDecimal avgPrice = getWeightedAvgPrice(item.getProductId());
            if (avgPrice.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = item.getUnitPrice().divide(avgPrice, 4, RoundingMode.HALF_UP);
                BigDecimal deviation = BigDecimal.ONE.subtract(ratio);
                item.setPriceDeviation(deviation);
                int level = detectAnomalyLevel(deviation);
                if (level > maxAnomalyLevel) maxAnomalyLevel = level;
                if (worstRatio == null || ratio.compareTo(worstRatio) < 0) worstRatio = ratio;
            }
            item.setAmount(item.getQuantity().multiply(item.getUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            total = total.add(item.getAmount());
        }

        if (maxAnomalyLevel >= 2 && (order.getLowPriceReason() == null || order.getLowPriceReason().isBlank())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "中度及以上价格异常必须填写低价特批理由");
        }

        order.setTotalAmount(total);
        order.setPriceAnomalyLevel(maxAnomalyLevel);
        order.setApprovalStatus(0);
        save(order);

        for (SalSalesOrderItem item : items) {
            item.setOrderId(order.getId());
            itemMapper.insert(item);
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("ratio", worstRatio == null ? 1.0 : worstRatio.doubleValue());
        workflowEngine.startWorkflow(WorkflowConstants.BusinessType.SALES_ORDER.getCode(), order.getId(), order.getSalespersonId(), metrics);
        log.info("销售报单已创建: orderNo={}, total={}, anomalyLevel={}, worstRatio={}", order.getOrderNo(), total, maxAnomalyLevel, worstRatio);
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
