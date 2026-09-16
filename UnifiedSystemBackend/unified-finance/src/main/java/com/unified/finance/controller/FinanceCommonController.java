package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.finance.entity.FinBudget;
import com.unified.finance.entity.FinAccount;
import com.unified.finance.entity.FinSupplier;
import com.unified.finance.service.BudgetService;
import com.unified.finance.mapper.FinAccountMapper;
import com.unified.finance.mapper.FinSupplierMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceCommonController {

    private final BudgetService budgetService;
    private final FinAccountMapper accountMapper;
    private final FinSupplierMapper supplierMapper;

    @GetMapping("/budgets")
    public Result<IPage<FinBudget>> listBudgets(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer year) {
        return Result.ok(budgetService.pageBudgets(pageNum, pageSize, deptId, year));
    }

    @PostMapping("/budgets")
    public Result<FinBudget> setBudget(@RequestBody FinBudget budget) {
        return Result.ok(budgetService.setBudget(budget));
    }

    @GetMapping("/accounts")
    public Result<IPage<FinAccount>> listAccounts(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(accountMapper.selectPage(new Page<>(pageNum, pageSize), null));
    }

    @PostMapping("/accounts")
    public Result<?> createAccount(@RequestBody FinAccount account) {
        accountMapper.insert(account);
        return Result.ok();
    }

    @GetMapping("/suppliers")
    public Result<IPage<FinSupplier>> listSuppliers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(supplierMapper.selectPage(new Page<>(pageNum, pageSize), null));
    }

    @PostMapping("/suppliers")
    public Result<?> createSupplier(@RequestBody FinSupplier supplier) {
        supplierMapper.insert(supplier);
        return Result.ok();
    }
}
