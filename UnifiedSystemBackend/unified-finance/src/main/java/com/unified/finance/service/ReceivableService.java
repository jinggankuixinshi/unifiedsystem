package com.unified.finance.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.finance.entity.FinReceivable;
import com.unified.finance.entity.FinReceivableLog;
import com.unified.finance.mapper.FinReceivableLogMapper;
import com.unified.finance.mapper.FinReceivableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceivableService extends ServiceImpl<FinReceivableMapper, FinReceivable> {

    private final FinReceivableLogMapper logMapper;

    @Transactional(rollbackFor = Exception.class)
    public FinReceivable createReceivable(FinReceivable receivable) {
        receivable.setReceivedAmount(BigDecimal.ZERO);
        receivable.setBalance(receivable.getAmount());
        receivable.setStatus(0);
        save(receivable);
        log.info("应收账款已登记: customerId={}, amount={}, dueDate={}", receivable.getCustomerId(), receivable.getAmount(), receivable.getDueDate());
        return receivable;
    }

    @Transactional(rollbackFor = Exception.class)
    public FinReceivable logPayment(Long receivableId, BigDecimal amount, String paymentMethod) {
        FinReceivable receivable = getById(receivableId);
        if (receivable == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "应收账款不存在");

        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(ErrorCode.AMOUNT_CHECK_FAIL.getCode(), "收款金额必须大于0");

        receivable.setReceivedAmount(receivable.getReceivedAmount().add(amount));
        receivable.setBalance(receivable.getBalance().subtract(amount));
        if (receivable.getBalance().compareTo(BigDecimal.ZERO) <= 0) receivable.setStatus(1);
        updateById(receivable);

        FinReceivableLog receivableLog = new FinReceivableLog();
        receivableLog.setReceivableId(receivableId);
        receivableLog.setAmount(amount);
        receivableLog.setPaymentDate(LocalDate.now());
        receivableLog.setPaymentMethod(paymentMethod);
        logMapper.insert(receivableLog);

        log.info("收款记录已登记: receivableId={}, amount={}, balance={}", receivableId, amount, receivable.getBalance());
        return receivable;
    }

    public IPage<FinReceivable> pageReceivables(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<FinReceivable> wrapper = new LambdaQueryWrapper<FinReceivable>()
                .eq(status != null, FinReceivable::getStatus, status)
                .orderByDesc(FinReceivable::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
