package com.unified.production.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.production.entity.ProdWorkOrder;

import java.math.BigDecimal;

public interface WorkOrderService extends IService<ProdWorkOrder> {

    ProdWorkOrder startWorkOrder(Long scheduleId, ProdWorkOrder order);

    ProdWorkOrder finishWorkOrder(Long orderId, BigDecimal actualOutput);

    IPage<ProdWorkOrder> pageOrders(int pageNum, int pageSize, Integer status);
}
