package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.constant.OrderStatusEnum;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.util.SequenceGenerator;
import com.unified.production.entity.ProdProductionSchedule;
import com.unified.production.entity.ProdWorkOrder;
import com.unified.production.mapper.ProdProductionScheduleMapper;
import com.unified.production.mapper.ProdWorkOrderMapper;
import com.unified.production.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@DS("production")
@RequiredArgsConstructor
public class WorkOrderServiceImpl extends ServiceImpl<ProdWorkOrderMapper, ProdWorkOrder> implements WorkOrderService {

    private final ProdProductionScheduleMapper scheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdWorkOrder startWorkOrder(Long scheduleId, ProdWorkOrder order) {
        ProdProductionSchedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "排期不存在");
        }

        order.setOrderNo(SequenceGenerator.generate("WO"));
        order.setScheduleId(scheduleId);
        order.setStartTime(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.IN_PROGRESS.getCode());
        save(order);
        log.info("工单已开工: orderNo={}, scheduleId={}", order.getOrderNo(), scheduleId);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdWorkOrder finishWorkOrder(Long orderId, BigDecimal actualOutput) {
        ProdWorkOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatusEnum.IN_PROGRESS.getCode()) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR.getCode(), "只有进行中的工单才能完工");
        }
        if (actualOutput == null || actualOutput.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "实际产出必须大于0");
        }

        order.setActualOutput(actualOutput);
        order.setEndTime(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        updateById(order);
        log.info("工单已完工: orderNo={}, actualOutput={}", order.getOrderNo(), actualOutput);
        return order;
    }

    @Override
    public IPage<ProdWorkOrder> pageOrders(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<ProdWorkOrder> wrapper = new LambdaQueryWrapper<ProdWorkOrder>()
                .eq(status != null, ProdWorkOrder::getStatus, status)
                .orderByDesc(ProdWorkOrder::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
