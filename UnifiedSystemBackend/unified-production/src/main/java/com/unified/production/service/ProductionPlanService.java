package com.unified.production.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.production.entity.ProdProductionPlan;
import com.unified.production.entity.ProdProductionSchedule;

import java.util.List;

public interface ProductionPlanService extends IService<ProdProductionPlan> {

    ProdProductionPlan createPlan(ProdProductionPlan plan);

    ProdProductionSchedule scheduleProduction(ProdProductionSchedule schedule);

    IPage<ProdProductionPlan> pagePlans(int pageNum, int pageSize, Integer status);

    List<ProdProductionSchedule> getSchedules(Long planId);
}
