package com.unified.finance.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.finance.entity.FinPayable;
import com.unified.finance.entity.FinPayableLog;
import com.unified.finance.mapper.FinPayableLogMapper;
import com.unified.finance.mapper.FinPayableMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@DS("finance")
@RequiredArgsConstructor
public class PayableService extends ServiceImpl<FinPayableMapper, FinPayable> {

    private final FinPayableLogMapper logMapper;

    @Transactional(rollbackFor = Exception.class)
    public FinPayable createPayable(FinPayable payable) {
        payable.setPaidAmount(BigDecimal.ZERO);
        payable.setBalance(payable.getAmount());
        payable.setStatus(0);
        save(payable);
        log.info("应付账款已登记: supplierId={}, amount={}, dueDate={}", payable.getSupplierId(), payable.getAmount(), payable.getDueDate());
        return payable;
    }

    @Transactional(rollbackFor = Exception.class)
    public FinPayable logPayment(Long payableId, BigDecimal amount, String paymentMethod) {
        FinPayable payable = getById(payableId);
        if (payable == null) throw new BusinessException(ErrorCode.ORDER_NOT_FOUND.getCode(), "应付账款不存在");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException(ErrorCode.AMOUNT_CHECK_FAIL.getCode(), "付款金额必须大于0");

        payable.setPaidAmount(payable.getPaidAmount().add(amount));
        payable.setBalance(payable.getBalance().subtract(amount));
        if (payable.getBalance().compareTo(BigDecimal.ZERO) <= 0) payable.setStatus(1);
        updateById(payable);

        FinPayableLog payableLog = new FinPayableLog();
        payableLog.setPayableId(payableId);
        payableLog.setAmount(amount);
        payableLog.setPaymentDate(LocalDate.now());
        payableLog.setPaymentMethod(paymentMethod);
        logMapper.insert(payableLog);

        log.info("付款记录已登记: payableId={}, amount={}, balance={}", payableId, amount, payable.getBalance());
        return payable;
    }

    public IPage<FinPayable> pagePayables(int pageNum, int pageSize, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<FinPayable> wrapper = new LambdaQueryWrapper<FinPayable>()
                .eq(status != null, FinPayable::getStatus, status)
                .orderByDesc(FinPayable::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
