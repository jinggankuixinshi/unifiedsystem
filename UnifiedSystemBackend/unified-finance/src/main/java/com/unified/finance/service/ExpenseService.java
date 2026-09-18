package com.unified.finance.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.security.UserContext;
import com.unified.common.util.SequenceGenerator;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEngine;
import com.unified.finance.entity.FinBudget;
import com.unified.finance.entity.FinExpense;
import com.unified.finance.entity.FinExpenseItem;
import com.unified.finance.mapper.FinBudgetMapper;
import com.unified.finance.mapper.FinExpenseItemMapper;
import com.unified.finance.mapper.FinExpenseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@DS("finance")
@RequiredArgsConstructor
public class ExpenseService extends ServiceImpl<FinExpenseMapper, FinExpense> {

    private final FinExpenseItemMapper itemMapper;
    private final FinBudgetMapper budgetMapper;
    private final WorkflowEngine workflowEngine;

    @DSTransactional(rollbackFor = Exception.class)
    public FinExpense createExpense(FinExpense expense, List<FinExpenseItem> items) {
        expense.setExpenseNo(SequenceGenerator.generate("EXP"));
        expense.setApplicantId(UserContext.get().getUserId());
        expense.setDeptId(UserContext.get().getDeptId());

        BigDecimal total = items.stream()
                .map(i -> i.getAmount() == null ? BigDecimal.ZERO : i.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        expense.setTotalAmount(total);
        expense.setApprovalStatus(0);
        save(expense);

        for (FinExpenseItem item : items) {
            item.setExpenseId(expense.getId());
            itemMapper.insert(item);
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("amount", total);
        workflowEngine.startWorkflow(WorkflowConstants.BusinessType.EXPENSE.getCode(), expense.getId(), expense.getApplicantId(), metrics);
        log.info("费用报销已提交: expenseNo={}, total={}, type={}", expense.getExpenseNo(), total, expense.getExpenseType());
        return expense;
    }

    public BigDecimal getBudgetCompare(Long deptId, Long subjectId, Integer year) {
        FinBudget budget = budgetMapper.selectOne(new LambdaQueryWrapper<FinBudget>()
                .eq(FinBudget::getDeptId, deptId)
                .eq(FinBudget::getSubjectId, subjectId)
                .eq(FinBudget::getBudgetYear, year));
        if (budget == null) return BigDecimal.ZERO;
        return budget.getExecutedAmount();
    }
}
