package com.unified.finance.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.finance.entity.FinBudget;
import com.unified.finance.entity.FinBudgetLog;
import com.unified.finance.mapper.FinBudgetLogMapper;
import com.unified.finance.mapper.FinBudgetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@DS("finance")
@RequiredArgsConstructor
public class BudgetService extends ServiceImpl<FinBudgetMapper, FinBudget> {

    private final FinBudgetLogMapper logMapper;

    @Transactional(rollbackFor = Exception.class)
    public FinBudget setBudget(FinBudget budget) {
        FinBudget existing = getOne(new LambdaQueryWrapper<FinBudget>()
                .eq(FinBudget::getDeptId, budget.getDeptId())
                .eq(FinBudget::getSubjectId, budget.getSubjectId())
                .eq(FinBudget::getBudgetYear, budget.getBudgetYear()));
        if (existing != null) {
            existing.setBudgetAmount(budget.getBudgetAmount());
            updateById(existing);
            return existing;
        }
        budget.setExecutedAmount(BigDecimal.ZERO);
        save(budget);
        return budget;
    }

    @Transactional(rollbackFor = Exception.class)
    public FinBudget executeBudget(Long budgetId, BigDecimal amount, Long businessId, String businessType) {
        FinBudget budget = getById(budgetId);
        if (budget == null) return null;

        budget.setExecutedAmount(budget.getExecutedAmount().add(amount));
        updateById(budget);

        FinBudgetLog log = new FinBudgetLog();
        log.setBudgetId(budgetId);
        log.setAmount(amount);
        log.setBusinessId(businessId);
        log.setBusinessType(businessType);
        logMapper.insert(log);

        return budget;
    }

    public IPage<FinBudget> pageBudgets(int pageNum, int pageSize, Long deptId, Integer year) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<FinBudget> wrapper = new LambdaQueryWrapper<FinBudget>()
                .eq(deptId != null, FinBudget::getDeptId, deptId)
                .eq(year != null, FinBudget::getBudgetYear, year)
                .orderByDesc(FinBudget::getBudgetYear);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
