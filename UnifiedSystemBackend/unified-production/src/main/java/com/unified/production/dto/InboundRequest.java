package com.unified.production.dto;

import com.unified.production.entity.ProdInbound;
import com.unified.production.entity.ProdInboundItem;
import lombok.Data;

import java.util.List;

@Data
public class InboundRequest {
    private ProdInbound inbound;
    private List<ProdInboundItem> items;
}
