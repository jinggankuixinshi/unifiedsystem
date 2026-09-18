package com.unified.system.workflow.strategy;

import com.unified.common.workflow.WorkflowApproverStrategy;
import com.unified.common.workflow.entity.WfNodeTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SpecificUserApproverStrategy implements WorkflowApproverStrategy {

    @Override
    public String approverType() {
        return "SPECIFIC_USER";
    }

    @Override
    public List<Long> resolve(WfNodeTemplate node, Long applicantId) {
        return node.getApproverId() == null ? Collections.emptyList() : List.of(node.getApproverId());
    }
}
