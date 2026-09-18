package com.unified.common.workflow.condition;

import org.springframework.stereotype.Component;

@Component
public class PercentageConditionEvaluator extends AbstractRangeConditionEvaluator {

    public PercentageConditionEvaluator() {
        super("PERCENTAGE", "ratio");
    }
}
