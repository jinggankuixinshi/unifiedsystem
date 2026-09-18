package com.unified.common.workflow.condition;

import org.springframework.stereotype.Component;

@Component
public class AmountRangeConditionEvaluator extends AbstractRangeConditionEvaluator {

    public AmountRangeConditionEvaluator() {
        super("AMOUNT_RANGE", "amount");
    }
}
