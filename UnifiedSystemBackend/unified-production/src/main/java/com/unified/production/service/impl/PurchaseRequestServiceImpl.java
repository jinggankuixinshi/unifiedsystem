package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.constant.ApprovalStatusEnum;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.common.util.SequenceGenerator;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEngine;
import com.unified.production.entity.ProdMaterial;
import com.unified.production.entity.ProdPurchaseRequest;
import com.unified.production.entity.ProdPurchaseRequestItem;
import com.unified.production.mapper.ProdPurchaseRequestItemMapper;
import com.unified.production.mapper.ProdPurchaseRequestMapper;
import com.unified.production.mapper.ProdMaterialMapper;
import com.unified.production.service.PurchaseRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("production")
public class PurchaseRequestServiceImpl extends ServiceImpl<ProdPurchaseRequestMapper, ProdPurchaseRequest> implements PurchaseRequestService {

    private final ProdPurchaseRequestItemMapper itemMapper;
    private final ProdMaterialMapper materialMapper;
    private final WorkflowEngine workflowEngine;

    @Override
    @DSTransactional(rollbackFor = Exception.class)
    public ProdPurchaseRequest createRequest(ProdPurchaseRequest request, List<ProdPurchaseRequestItem> items) {
        request.setRequestNo(SequenceGenerator.generate("PR"));
        UserContext ctx = UserContext.get();
        if (ctx != null) {
            request.setApplicantId(ctx.getUserId());
            request.setDeptId(ctx.getDeptId());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (ProdPurchaseRequestItem item : items) {
            ProdMaterial material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND.getCode(),
                        "物料不存在: " + item.getMaterialId());
            }
            item.setAmount(item.getQuantity().multiply(item.getUnitPrice()));
            total = total.add(item.getAmount());
        }
        request.setTotalAmount(total);
        request.setApprovalStatus(ApprovalStatusEnum.PENDING.getCode());
        save(request);

        items.forEach(item -> item.setRequestId(request.getId()));
        for (ProdPurchaseRequestItem item : items) {
            itemMapper.insert(item);
        }

        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        metrics.put("amount", total);
        workflowEngine.startWorkflow(WorkflowConstants.BusinessType.PURCHASE_REQUEST.getCode(), request.getId(), request.getApplicantId(), metrics);
        log.info("采购申请已创建: requestNo={}, totalAmount={}, userId={}", request.getRequestNo(), total, request.getApplicantId());
        return request;
    }

    @Override
    public IPage<ProdPurchaseRequest> pageRequests(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<ProdPurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ProdPurchaseRequest::getApprovalStatus, status);
        }
        wrapper.orderByDesc(ProdPurchaseRequest::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<ProdPurchaseRequestItem> getRequestItems(Long requestId) {
        return itemMapper.selectList(new LambdaQueryWrapper<ProdPurchaseRequestItem>()
                .eq(ProdPurchaseRequestItem::getRequestId, requestId));
    }
}
