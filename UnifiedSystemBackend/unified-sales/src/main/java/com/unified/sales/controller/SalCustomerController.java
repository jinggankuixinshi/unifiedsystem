package com.unified.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.sales.entity.SalCustomer;
import com.unified.sales.service.SalCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales/customers")
@RequiredArgsConstructor
public class SalCustomerController {

    private final SalCustomerService customerService;

    @GetMapping
    public Result<Page<SalCustomer>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SalCustomer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SalCustomer::getCustomerName, keyword)
                   .or().like(SalCustomer::getCustomerCode, keyword);
        }
        return Result.ok(customerService.page(new Page<>(page, pageSize), wrapper));
    }

    @GetMapping("/{id}")
    public Result<SalCustomer> getById(@PathVariable Long id) {
        return Result.ok(customerService.getById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody SalCustomer customer) {
        customerService.save(customer);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody SalCustomer customer) {
        customer.setId(id);
        customerService.updateById(customer);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return Result.ok();
    }
}
