package com.unified.production.dto;

import com.unified.production.entity.ProdBom;
import com.unified.production.entity.ProdBomItem;
import lombok.Data;

import java.util.List;

@Data
public class BomRequest {
    private ProdBom bom;
    private List<ProdBomItem> items;
}
