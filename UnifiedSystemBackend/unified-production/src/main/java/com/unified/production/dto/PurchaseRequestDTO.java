package com.unified.production.dto;

import com.unified.production.entity.ProdPurchaseRequest;
import com.unified.production.entity.ProdPurchaseRequestItem;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseRequestDTO {
    private ProdPurchaseRequest request;
    private List<ProdPurchaseRequestItem> items;
}
