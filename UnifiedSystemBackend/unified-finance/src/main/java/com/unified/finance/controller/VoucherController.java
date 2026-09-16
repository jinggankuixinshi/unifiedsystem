package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.finance.entity.FinVoucher;
import com.unified.finance.entity.FinVoucherEntry;
import com.unified.finance.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public Result<IPage<FinVoucher>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        return Result.ok(voucherService.page(new Page<>(pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public Result<FinVoucher> getById(@PathVariable Long id) {
        return Result.ok(voucherService.getById(id));
    }

    @GetMapping("/{id}/entries")
    public Result<List<FinVoucherEntry>> getEntries(@PathVariable Long id) {
        return Result.ok(voucherService.getEntries(id));
    }

    @PostMapping
    public Result<FinVoucher> create(@RequestParam String summary,
                                      @RequestBody List<FinVoucherEntry> entries) {
        return Result.ok(voucherService.createVoucher(summary, entries));
    }
}
