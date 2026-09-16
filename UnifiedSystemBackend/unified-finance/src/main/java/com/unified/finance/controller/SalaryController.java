package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.finance.entity.FinSalary;
import com.unified.finance.service.SalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @GetMapping
    public Result<IPage<FinSalary>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(salaryService.page(new Page<>(pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public Result<FinSalary> getById(@PathVariable Long id) {
        return Result.ok(salaryService.getById(id));
    }

    @PostMapping
    public Result<FinSalary> create(@RequestBody FinSalary salary) {
        return Result.ok(salaryService.calculateSalary(salary));
    }

    @PutMapping("/{id}")
    public Result<FinSalary> update(@PathVariable Long id, @RequestBody FinSalary salary) {
        salary.setId(id);
        return Result.ok(salaryService.calculateSalary(salary));
    }
}
