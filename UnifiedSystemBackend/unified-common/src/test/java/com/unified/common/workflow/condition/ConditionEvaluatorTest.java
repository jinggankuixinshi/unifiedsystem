package com.unified.common.workflow.condition;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionEvaluatorTest {

    private final AmountRangeConditionEvaluator amount = new AmountRangeConditionEvaluator();
    private final PercentageConditionEvaluator percentage = new PercentageConditionEvaluator();
    private final ApplicantLevelConditionEvaluator applicantLevel = new ApplicantLevelConditionEvaluator();
    private final TypeConditionEvaluator type = new TypeConditionEvaluator();
    private final NoneConditionEvaluator none = new NoneConditionEvaluator();

    @Test
    void amountRangeMinInclusiveMaxExclusive() {
        String cfg = "{\"min\":5000,\"max\":50000}";
        assertFalse(amount.evaluate(cfg, Map.of("amount", 4999)));
        assertTrue(amount.evaluate(cfg, Map.of("amount", 5000)));
        assertTrue(amount.evaluate(cfg, Map.of("amount", 49999)));
        assertFalse(amount.evaluate(cfg, Map.of("amount", 50000)));
    }

    @Test
    void amountRangeOpenEnds() {
        assertTrue(amount.evaluate("{\"max\":5000}", Map.of("amount", 0)));
        assertFalse(amount.evaluate("{\"max\":5000}", Map.of("amount", 5000)));
        assertTrue(amount.evaluate("{\"min\":200000}", Map.of("amount", 200000)));
        assertFalse(amount.evaluate("{\"min\":200000}", Map.of("amount", 199999)));
    }

    @Test
    void amountRangeWithoutMetricIsFalse() {
        assertFalse(amount.evaluate("{\"max\":5000}", Map.of()));
    }

    @Test
    void typesFilterAppliesToAmountRange() {
        String cfg = "{\"max\":50000,\"types\":[\"normal\"]}";
        assertTrue(amount.evaluate(cfg, Map.of("amount", 100, "type", "normal")));
        assertFalse(amount.evaluate(cfg, Map.of("amount", 100, "type", "sample")));
        assertFalse(amount.evaluate(cfg, Map.of("amount", 100)));
    }

    @Test
    void percentageBranches() {
        assertTrue(percentage.evaluate("{\"min\":0.85}", Map.of("ratio", 0.9)));
        assertTrue(percentage.evaluate("{\"min\":0.85}", Map.of("ratio", 0.85)));
        assertFalse(percentage.evaluate("{\"min\":0.70,\"max\":0.85}", Map.of("ratio", 0.85)));
        assertTrue(percentage.evaluate("{\"min\":0.70,\"max\":0.85}", Map.of("ratio", 0.84)));
        assertFalse(percentage.evaluate("{\"min\":0.70,\"max\":0.85}", Map.of("ratio", 0.69)));
        assertFalse(percentage.evaluate("{\"max\":0.50}", Map.of("ratio", 0.50)));
    }

    @Test
    void applicantLevelBranches() {
        assertTrue(applicantLevel.evaluate("{\"max\":60}", Map.of("applicantLevel", 30)));
        assertFalse(applicantLevel.evaluate("{\"max\":60}", Map.of("applicantLevel", 60)));
        assertTrue(applicantLevel.evaluate("{\"min\":60,\"max\":80}", Map.of("applicantLevel", 60)));
        assertTrue(applicantLevel.evaluate("{\"min\":80}", Map.of("applicantLevel", 100)));
    }

    @Test
    void typeMatch() {
        String cfg = "{\"types\":[\"sample\",\"scrap\"]}";
        assertTrue(type.evaluate(cfg, Map.of("type", "scrap")));
        assertFalse(type.evaluate(cfg, Map.of("type", "normal")));
        assertFalse(type.evaluate("{}", Map.of("type", "scrap")));
    }

    @Test
    void noneAlwaysTrue() {
        assertTrue(none.evaluate(null, Map.of()));
        assertTrue(none.evaluate("{}", Map.of("amount", 1)));
    }
}
