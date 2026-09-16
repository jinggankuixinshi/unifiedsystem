package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.finance.entity.FinReceivable;
import com.unified.finance.service.ReceivableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/finance/receivables")
@RequiredArgsConstructor
public class ReceivableController {

    private final ReceivableService receivableService;

    @GetMapping
    public Result<IPage<FinReceivable>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(receivableService.pageReceivables(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<FinReceivable> getById(@PathVariable Long id) {
        return Result.ok(receivableService.getById(id));
    }

    @PostMapping
    public Result<FinReceivable> create(@RequestBody FinReceivable receivable) {
        return Result.ok(receivableService.createReceivable(receivable));
    }

    @PostMapping("/{id}/payment")
    public Result<FinReceivable> payment(@PathVariable Long id,
                                          @RequestParam BigDecimal amount,
                                          @RequestParam(defaultValue = "bank") String method) {
        return Result.ok(receivableService.logPayment(id, amount, method));
    }
}
