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
import com.unified.production.entity.ProdProductionPlan;
import com.unified.production.entity.ProdProductionSchedule;
import com.unified.production.mapper.ProdProductionPlanMapper;
import com.unified.production.mapper.ProdProductionScheduleMapper;
import com.unified.production.service.ProductionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("production")
public class ProductionPlanServiceImpl extends ServiceImpl<ProdProductionPlanMapper, ProdProductionPlan> implements ProductionPlanService {

    private final ProdProductionScheduleMapper scheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdProductionPlan createPlan(ProdProductionPlan plan) {
        plan.setPlanNo(SequenceGenerator.generate("PP"));
        plan.setStatus(OrderStatusEnum.PENDING.getCode());
        save(plan);
        log.info("生产计划已创建: planNo={}, productId={}", plan.getPlanNo(), plan.getProductId());
        return plan;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdProductionSchedule scheduleProduction(ProdProductionSchedule schedule) {
        ProdProductionPlan plan = getById(schedule.getPlanId());
        if (plan == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "生产计划不存在");
        }
        if (plan.getStatus() == null || plan.getStatus() != OrderStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR.getCode(), "只有待排期的计划才能进行排期");
        }
        if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
            long conflictCount = scheduleMapper.selectCount(new LambdaQueryWrapper<ProdProductionSchedule>()
                    .eq(ProdProductionSchedule::getProductionLine, schedule.getProductionLine())
                    .lt(ProdProductionSchedule::getStartTime, schedule.getEndTime())
                    .gt(ProdProductionSchedule::getEndTime, schedule.getStartTime()));
            if (conflictCount > 0) {
                throw new BusinessException(ErrorCode.CONFLICT.getCode(),
                        "排期时间冲突：产线 " + schedule.getProductionLine() + " 在该时间段已被占用");
            }
        }

        scheduleMapper.insert(schedule);
        plan.setStatus(OrderStatusEnum.IN_PROGRESS.getCode());
        updateById(plan);
        log.info("排期完成: planId={}, productionLine={}, team={}", schedule.getPlanId(), schedule.getProductionLine(), schedule.getTeam());
        return schedule;
    }

    @Override
    public IPage<ProdProductionPlan> pagePlans(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<ProdProductionPlan> wrapper = new LambdaQueryWrapper<ProdProductionPlan>()
                .eq(status != null, ProdProductionPlan::getStatus, status)
                .orderByDesc(ProdProductionPlan::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<ProdProductionSchedule> getSchedules(Long planId) {
        return scheduleMapper.selectList(new LambdaQueryWrapper<ProdProductionSchedule>()
                .eq(ProdProductionSchedule::getPlanId, planId)
                .orderByAsc(ProdProductionSchedule::getStartTime));
    }
}
