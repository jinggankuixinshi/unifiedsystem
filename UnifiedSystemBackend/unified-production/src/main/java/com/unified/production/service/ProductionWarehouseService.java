package com.unified.production.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.production.entity.ProdInbound;
import com.unified.production.entity.ProdInboundItem;
import com.unified.production.entity.ProdOutbound;
import com.unified.production.entity.ProdOutboundItem;
import com.unified.production.entity.ProdWarehouse;

import java.util.List;

public interface ProductionWarehouseService extends IService<ProdWarehouse> {

    ProdInbound createInbound(ProdInbound inbound, List<ProdInboundItem> items);

    ProdOutbound createOutbound(ProdOutbound outbound, List<ProdOutboundItem> items);
}
