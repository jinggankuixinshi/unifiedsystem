package com.unified.report.controller;

import com.unified.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 报表控制器 — JimuReport 引擎集成预留
 * 复杂报表生成异步执行，完成后通过 WebSocket/消息通知用户
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    @PostMapping("/generate/{reportCode}")
    public Result<?> generate(@PathVariable String reportCode, @RequestBody(required = false) java.util.Map<String, Object> params) {
        return Result.ok("报表生成请求已提交: " + reportCode + ", 完成后将通知您下载");
    }

    @GetMapping("/list")
    public Result<?> listAvailable() {
        return Result.ok(new String[][]{
            {"profit_statement", "利润表（损益表）", "月报/季报/年报"},
            {"balance_sheet", "资产负债表", "月报/季报/年报"},
            {"cash_flow", "现金流量表", "月报/季报/年报"},
            {"sales_trend", "销售趋势报表", "月报/季报"},
            {"inventory_turnover", "库存周转报表", "月报"},
            {"receivable_aging", "应收账龄报表", "月报"},
            {"production_cost", "生产成本报表", "月报"},
            {"expense_summary", "费用汇总报表", "月报"}
        });
    }
}
