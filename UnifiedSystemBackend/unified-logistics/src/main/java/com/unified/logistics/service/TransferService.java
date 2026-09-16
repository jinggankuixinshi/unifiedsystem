package com.unified.logistics.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.common.util.SequenceGenerator;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEngine;
import com.unified.logistics.entity.*;
import com.unified.logistics.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨管理服务
 * 按类型和价值分级流转审批
 * 常规：制单→调入仓主管审核→按金额分级→实物交接→签收归档
 * 特殊：样品/报废/返修/赠送 → 额外质检/财务/老板审批
 */
@Slf4j
@Service
@DS("logistics")
@RequiredArgsConstructor
public class TransferService extends ServiceImpl<LogTransferMapper, LogTransfer> {

    private final LogTransferItemMapper itemMapper;
    private final LogTransferSignMapper signMapper;
    private final WorkflowEngine workflowEngine;

    @DSTransactional(rollbackFor = Exception.class)
    public LogTransfer createTransfer(LogTransfer transfer, List<LogTransferItem> items) {
        transfer.setTransferNo(SequenceGenerator.generate("TR"));
        transfer.setApprovalStatus(0);

        for (LogTransferItem item : items) {
            if (item.getUnitValue() == null) item.setUnitValue(BigDecimal.ZERO);
            if (item.getQuantity() == null) item.setQuantity(BigDecimal.ZERO);
        }
        BigDecimal totalValue = items.stream()
                .map(i -> i.getUnitValue().multiply(i.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        transfer.setTotalValue(totalValue);
        save(transfer);

        for (LogTransferItem item : items) {
            item.setTransferId(transfer.getId());
            itemMapper.insert(item);
        }

        workflowEngine.startWorkflow(WorkflowConstants.BusinessType.TRANSFER.getCode(), transfer.getId());
        log.info("调拨单已创建: transferNo={}, type={}, totalValue={}", transfer.getTransferNo(), transfer.getType(), totalValue);
        return transfer;
    }

    @Transactional(rollbackFor = Exception.class)
    public LogTransferSign signTransfer(Long transferId) {
        LogTransfer transfer = getById(transferId);
        if (transfer == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "调拨单不存在");

        LogTransferSign sign = new LogTransferSign();
        sign.setTransferId(transferId);
        sign.setSignerId(UserContext.get().getUserId());
        sign.setSignTime(LocalDateTime.now());
        sign.setSignType("confirm");
        signMapper.insert(sign);

        log.info("调拨签收: transferId={}, signerId={}", transferId, sign.getSignerId());
        return sign;
    }

    public IPage<LogTransfer> pageTransfers(int pageNum, int pageSize, String type, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<LogTransfer> wrapper = new LambdaQueryWrapper<LogTransfer>()
                .eq(type != null, LogTransfer::getType, type)
                .eq(status != null, LogTransfer::getApprovalStatus, status)
                .orderByDesc(LogTransfer::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<LogTransferItem> getItems(Long transferId) {
        return itemMapper.selectList(new LambdaQueryWrapper<LogTransferItem>()
                .eq(LogTransferItem::getTransferId, transferId));
    }
}
