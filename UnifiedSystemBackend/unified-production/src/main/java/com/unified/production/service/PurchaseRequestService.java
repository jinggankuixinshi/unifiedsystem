package com.unified.production.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.production.entity.ProdPurchaseRequest;
import com.unified.production.entity.ProdPurchaseRequestItem;

import java.util.List;

public interface PurchaseRequestService extends IService<ProdPurchaseRequest> {

    ProdPurchaseRequest createRequest(ProdPurchaseRequest request, List<ProdPurchaseRequestItem> items);

    IPage<ProdPurchaseRequest> pageRequests(int pageNum, int pageSize, Integer status);

    List<ProdPurchaseRequestItem> getRequestItems(Long requestId);
}
