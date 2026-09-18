package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.finance.dto.ExpenseCreateDTO;
import com.unified.finance.entity.FinExpense;
import com.unified.finance.entity.FinExpenseItem;
import com.unified.finance.service.ExpenseService;
import jakarta.validation.Valid;
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
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FinExpense> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(FinExpense::getApprovalStatus, status);
        }
        wrapper.orderByDesc(FinExpense::getCreateTime);
        return Result.ok(expenseService.page(new Page<>(pageNum, pageSize), wrapper));
    }

    @GetMapping("/{id}")
    public Result<FinExpense> getById(@PathVariable Long id) {
        return Result.ok(expenseService.getById(id));
    }

    @PostMapping
    public Result<FinExpense> create(@Valid @RequestBody ExpenseCreateDTO dto) {
        List<FinExpenseItem> items = dto.getItems() == null ? List.of() : dto.getItems();
        return Result.ok(expenseService.createExpense(dto.getExpense(), items));
    }

    @GetMapping("/budget-compare")
    public Result<java.util.Map<String, Object>> budgetCompare(
            @RequestParam Long deptId, @RequestParam Long subjectId,
            @RequestParam(defaultValue = "2026") Integer year) {
        return Result.ok(java.util.Map.of("executedAmount", expenseService.getBudgetCompare(deptId, subjectId, year)));
    }
}
