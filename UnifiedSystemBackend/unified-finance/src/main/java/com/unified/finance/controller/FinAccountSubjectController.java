package com.unified.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unified.common.core.Result;
import com.unified.finance.entity.FinAccountSubject;
import com.unified.finance.service.FinAccountSubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/subjects")
@RequiredArgsConstructor
public class FinAccountSubjectController {

    private final FinAccountSubjectService subjectService;

    @GetMapping
    public Result<Page<FinAccountSubject>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(subjectService.page(new Page<>(page, pageSize)));
    }

    @GetMapping("/{id}")
    public Result<FinAccountSubject> getById(@PathVariable Long id) {
        return Result.ok(subjectService.getById(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody FinAccountSubject subject) {
        subjectService.save(subject);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody FinAccountSubject subject) {
        subject.setId(id);
        subjectService.updateById(subject);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        subjectService.removeById(id);
        return Result.ok();
    }
}
