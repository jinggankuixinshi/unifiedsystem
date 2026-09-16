package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.finance.entity.FinExpense;
import com.unified.finance.entity.FinExpenseItem;
import com.unified.finance.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public Result<IPage<FinExpense>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(expenseService.page(new Page<>(pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public Result<FinExpense> getById(@PathVariable Long id) {
        return Result.ok(expenseService.getById(id));
    }

    @PostMapping
    public Result<FinExpense> create(@RequestBody FinExpense expense) {
        return Result.ok(expenseService.createExpense(expense, List.of()));
    }

    @GetMapping("/budget-compare")
    public Result<java.util.Map<String, Object>> budgetCompare(
            @RequestParam Long deptId, @RequestParam Long subjectId,
            @RequestParam(defaultValue = "2026") Integer year) {
        return Result.ok(java.util.Map.of("executedAmount", expenseService.getBudgetCompare(deptId, subjectId, year)));
    }
}
