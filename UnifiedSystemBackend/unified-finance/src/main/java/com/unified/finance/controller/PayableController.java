package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.unified.common.core.Result;
import com.unified.finance.dto.PaymentDTO;
import com.unified.finance.entity.FinPayable;
import com.unified.finance.service.PayableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/payables")
@RequiredArgsConstructor
public class PayableController {

    private final PayableService payableService;

    @GetMapping
    public Result<IPage<FinPayable>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.ok(payableService.pagePayables(pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<FinPayable> getById(@PathVariable Long id) {
        return Result.ok(payableService.getById(id));
    }

    @PostMapping
    public Result<FinPayable> create(@RequestBody FinPayable payable) {
        return Result.ok(payableService.createPayable(payable));
    }

    @PostMapping("/{id}/payment")
    public Result<FinPayable> payment(@PathVariable Long id, @Valid @RequestBody PaymentDTO dto) {
        return Result.ok(payableService.logPayment(id, dto.getAmount(), dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "bank"));
    }
}
