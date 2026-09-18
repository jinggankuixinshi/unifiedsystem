package com.unified.common.workflow.condition;

import org.springframework.stereotype.Component;

@Component
public class ApplicantLevelConditionEvaluator extends AbstractRangeConditionEvaluator {

    public ApplicantLevelConditionEvaluator() {
        super("APPLICANT_LEVEL", "applicantLevel");
    }
}
