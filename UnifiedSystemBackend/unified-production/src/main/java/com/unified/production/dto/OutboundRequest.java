package com.unified.production.dto;

import com.unified.production.entity.ProdOutbound;
import com.unified.production.entity.ProdOutboundItem;
import lombok.Data;

import java.util.List;

@Data
public class OutboundRequest {
    private ProdOutbound outbound;
    private List<ProdOutboundItem> items;
}
