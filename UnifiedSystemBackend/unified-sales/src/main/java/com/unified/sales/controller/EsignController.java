package com.unified.sales.controller;

import com.unified.common.core.Result;
import com.unified.sales.service.EsignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * e签宝回调接口 — Spring Security 放行，由服务内部验签
 */
@RestController
@RequestMapping("/api/esign")
@RequiredArgsConstructor
public class EsignController {

    private final EsignService esignService;

    @PostMapping("/initiate")
    public Result<?> initiate(@RequestParam String contractNo,
                               @RequestParam String contentJson,
                               @RequestBody(required = false) Map<String, String> signers) {
        String taskId = esignService.createSignTask(contractNo, contentJson, signers);
        return Result.ok(Map.of("taskId", taskId));
    }

    @GetMapping("/status/{taskId}")
    public Result<?> status(@PathVariable String taskId) {
        return Result.ok(Map.of("status", esignService.querySignStatus(taskId)));
    }

    @PostMapping("/cancel/{taskId}")
    public Result<?> cancel(@PathVariable String taskId) {
        esignService.cancelSignTask(taskId);
        return Result.ok();
    }

    /**
     * e签宝回调接口 — 需在 SecurityConfig 中放行
     */
    @PostMapping("/callback")
    public Result<?> callback(@RequestBody String callbackData,
                               @RequestHeader(value = "X-Esign-Signature", required = false) String signature) {
        esignService.handleCallback(callbackData, signature != null ? signature : "");
        return Result.ok();
    }
}
