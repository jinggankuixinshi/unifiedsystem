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
import java.util.stream.Collectors;

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

    /** 待办（当前节点由我审批） */
    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WfInstance>> pendingList(@RequestParam(required = false) String businessType) {
        List<WfInstance> list = workflowEngine.listPendingByApprover(UserContext.get().getUserId());
        if (businessType != null && !businessType.isEmpty()) {
            list = list.stream()
                    .filter(i -> businessType.equals(i.getBusinessType()))
                    .collect(Collectors.toList());
        }
        return Result.ok(list);
    }

    /** 已办（我操作过的） */
    @GetMapping("/done")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WfInstance>> doneList() {
        return Result.ok(workflowEngine.listDoneByApprover(UserContext.get().getUserId()));
    }

    /** 我的申请 */
    @GetMapping("/my-applications")
    @PreAuthorize("isAuthenticated()")
    public Result<List<WfInstance>> myApplications() {
        return Result.ok(workflowEngine.listMyApplications(UserContext.get().getUserId()));
    }

    @PostMapping("/instances/{id}/approve")
    @PreAuthorize("isAuthenticated()")
    public Result<?> approve(@PathVariable Long id, @RequestParam(defaultValue = "同意") String comment) {
        Long approverId = UserContext.get().getUserId();
        workflowEngine.approve(id, approverId, WorkflowConstants.ApprovalAction.APPROVE, comment);
        return Result.ok();
    }

    @PostMapping("/instances/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public Result<?> reject(@PathVariable Long id, @RequestParam(defaultValue = "驳回") String comment) {
        Long approverId = UserContext.get().getUserId();
        workflowEngine.approve(id, approverId, WorkflowConstants.ApprovalAction.REJECT, comment);
        return Result.ok();
    }

    @PostMapping("/instances/{id}/push-up")
    @PreAuthorize("isAuthenticated()")
    public Result<?> pushUp(@PathVariable Long id, @RequestParam(defaultValue = "上推审批") String comment) {
        Long approverId = UserContext.get().getUserId();
        workflowEngine.approve(id, approverId, WorkflowConstants.ApprovalAction.PUSH_UP, comment);
        return Result.ok();
    }

    @PostMapping("/instances/{id}/delegate")
    @PreAuthorize("isAuthenticated()")
    public Result<?> delegate(@PathVariable Long id,
                              @RequestParam Long targetUserId,
                              @RequestParam(defaultValue = "") String comment) {
        workflowEngine.delegate(id, UserContext.get().getUserId(), targetUserId, comment);
        return Result.ok();
    }

    @PostMapping("/instances/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Result<?> cancel(@PathVariable Long id) {
        workflowEngine.cancel(id, UserContext.get().getUserId());
        return Result.ok();
    }
}
