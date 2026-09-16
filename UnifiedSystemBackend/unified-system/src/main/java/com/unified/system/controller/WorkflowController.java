package com.unified.system.controller;

import com.unified.common.core.Result;
import com.unified.common.security.UserContext;
import com.unified.common.workflow.WorkflowConstants;
import com.unified.common.workflow.WorkflowEngine;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.common.workflow.entity.WfRecord;
import com.unified.common.workflow.entity.WfTemplate;
import com.unified.system.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowEngine workflowEngine;
    private final WfTemplateService templateService;
    private final WfNodeTemplateService nodeTemplateService;
    private final WfInstanceService instanceService;
    private final WfRecordService recordService;

    @GetMapping("/templates")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WfTemplate>> listTemplates() {
        return Result.ok(templateService.list());
    }

    @GetMapping("/templates/{id}/nodes")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WfNodeTemplate>> getNodes(@PathVariable Long id) {
        return Result.ok(nodeTemplateService.listByTemplateId(id));
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('admin')")
    public Result<WfTemplate> createTemplate(@RequestBody WfTemplate template) {
        templateService.save(template);
        return Result.ok(template);
    }

    @PostMapping("/templates/{id}/nodes")
    @PreAuthorize("hasRole('admin')")
    public Result<?> saveNodes(@PathVariable Long id, @RequestBody List<WfNodeTemplate> nodes) {
        for (WfNodeTemplate node : nodes) {
            node.setTemplateId(id);
            nodeTemplateService.save(node);
        }
        return Result.ok();
    }

    @GetMapping("/instances/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<WfInstance> getInstance(@PathVariable Long id) {
        return Result.ok(instanceService.getById(id));
    }

    @GetMapping("/instances/by-business")
    @PreAuthorize("isAuthenticated()")
    public Result<WfInstance> getInstanceByBusiness(@RequestParam String businessType, @RequestParam Long businessId) {
        return Result.ok(instanceService.getByBusiness(businessType, businessId));
    }

    @GetMapping("/instances/{id}/records")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WfRecord>> getRecords(@PathVariable Long id) {
        return Result.ok(recordService.listByInstanceId(id));
    }

    @PostMapping("/instances/{id}/approve")
    @PreAuthorize("hasAnyRole('admin', 'boss')")
    public Result<?> approve(@PathVariable Long id, @RequestParam(defaultValue = "同意") String comment) {
        Long approverId = UserContext.get().getUserId();
        workflowEngine.approve(id, approverId, WorkflowConstants.ApprovalAction.APPROVE, comment);
        return Result.ok();
    }

    @PostMapping("/instances/{id}/reject")
    @PreAuthorize("hasAnyRole('admin', 'boss')")
    public Result<?> reject(@PathVariable Long id, @RequestParam(defaultValue = "驳回") String comment) {
        Long approverId = UserContext.get().getUserId();
        workflowEngine.approve(id, approverId, WorkflowConstants.ApprovalAction.REJECT, comment);
        return Result.ok();
    }

    @PostMapping("/instances/{id}/push-up")
    @PreAuthorize("hasAnyRole('admin', 'boss')")
    public Result<?> pushUp(@PathVariable Long id, @RequestParam(defaultValue = "上推审批") String comment) {
        Long approverId = UserContext.get().getUserId();
        workflowEngine.approve(id, approverId, WorkflowConstants.ApprovalAction.PUSH_UP, comment);
        return Result.ok();
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public Result<?> pendingList(@RequestParam(required = false) String businessType) {
        if (businessType != null && !businessType.isEmpty()) {
            return Result.ok(instanceService.listByBusinessType(businessType));
        }
        return Result.ok(instanceService.list());
    }
}
