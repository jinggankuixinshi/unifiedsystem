package com.unified.system.controller;

import com.unified.common.core.Result;
import com.unified.system.entity.SysDictData;
import com.unified.system.entity.SysDictType;
import com.unified.system.service.SysDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class SysDictController {

    private final SysDictService dictService;

    @GetMapping("/types")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictType>> listTypes() {
        return Result.ok(dictService.list());
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('admin')")
    public Result<?> saveType(@Valid @RequestBody SysDictType dictType) {
        dictService.save(dictType);
        return Result.ok();
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> updateType(@PathVariable Long id, @Valid @RequestBody SysDictType dictType) {
        dictType.setId(id);
        dictService.updateById(dictType);
        return Result.ok();
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> deleteType(@PathVariable Long id) {
        dictService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/data")
    @PreAuthorize("isAuthenticated()")
    public Result<List<SysDictData>> listData(@RequestParam String type) {
        return Result.ok(dictService.getDictDataByType(type));
    }

    @PostMapping("/data")
    @PreAuthorize("hasRole('admin')")
    public Result<?> saveData(@Valid @RequestBody SysDictData data) {
        dictService.saveDictData(data);
        return Result.ok();
    }

    @PutMapping("/data/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> updateData(@PathVariable Long id, @Valid @RequestBody SysDictData data) {
        data.setId(id);
        dictService.updateDictData(data);
        return Result.ok();
    }

    @DeleteMapping("/data/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<?> deleteData(@PathVariable Long id) {
        dictService.deleteDictData(id);
        return Result.ok();
    }
}
