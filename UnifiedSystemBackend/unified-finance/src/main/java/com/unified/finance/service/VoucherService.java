package com.unified.finance.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.common.util.SequenceGenerator;
import com.unified.finance.entity.FinVoucher;
import com.unified.finance.entity.FinVoucherEntry;
import com.unified.finance.mapper.FinVoucherEntryMapper;
import com.unified.finance.mapper.FinVoucherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@DS("finance")
@RequiredArgsConstructor
public class VoucherService extends ServiceImpl<FinVoucherMapper, FinVoucher> {

    private final FinVoucherEntryMapper entryMapper;

    @Transactional(rollbackFor = Exception.class)
    public FinVoucher createVoucher(LocalDate voucherDate, String summary, List<FinVoucherEntry> entries) {
        BigDecimal totalDebit = entries.stream()
                .map(e -> e.getDebitAmount() == null ? BigDecimal.ZERO : e.getDebitAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = entries.stream()
                .map(e -> e.getCreditAmount() == null ? BigDecimal.ZERO : e.getCreditAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BusinessException(ErrorCode.AMOUNT_CHECK_FAIL.getCode(), "借贷金额不平衡: 借方=" + totalDebit + ", 贷方=" + totalCredit);
        }

        FinVoucher voucher = new FinVoucher();
        voucher.setVoucherNo(SequenceGenerator.generate("VCH"));
        voucher.setVoucherDate(voucherDate != null ? voucherDate : LocalDate.now());
        voucher.setSummary(summary);
        voucher.setCreatorId(UserContext.get().getUserId());
        voucher.setStatus(1);
        save(voucher);

        for (FinVoucherEntry entry : entries) {
            if (entry.getDebitAmount() == null) entry.setDebitAmount(BigDecimal.ZERO);
            if (entry.getCreditAmount() == null) entry.setCreditAmount(BigDecimal.ZERO);
            entry.setVoucherId(voucher.getId());
            entryMapper.insert(entry);
        }

        log.info("记账凭证已创建: voucherNo={}, 借方总额={}, 贷方总额={}", voucher.getVoucherNo(), totalDebit, totalCredit);
        return voucher;
    }

    public List<FinVoucherEntry> getEntries(Long voucherId) {
        return entryMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FinVoucherEntry>()
                .eq(FinVoucherEntry::getVoucherId, voucherId));
    }
}
