package com.unified.common.workflow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface WorkflowConstants {

    enum BusinessType {
        PURCHASE_REQUEST("purchase_request", "采购申请"),
        SALES_ORDER("sales_order", "销售报单"),
        TRANSFER("transfer", "调拨单"),
        EXPENSE("expense", "费用报销"),
        LEAVE("leave", "请假申请"),
        OVERTIME("overtime", "加班申请");

        private final String code;
        private final String name;

        BusinessType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
    }

    enum ApproverType {
        DEPT_MANAGER("部门经理"),
        ROLE("指定角色"),
        SPECIFIC_USER("指定用户"),
        POSITION("指定职级");

        private final String name;

        ApproverType(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    enum ConditionType {
        NONE("无条件"),
        AMOUNT_RANGE("金额区间"),
        PERCENTAGE("比例阈值"),
        TYPE("类型匹配");

        private final String name;

        ConditionType(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    enum ApprovalAction {
        APPROVE("通过"),
        REJECT("驳回"),
        PUSH_UP("上推"),
        DELEGATE("委托");

        private final String name;

        ApprovalAction(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    enum InstanceStatus {
        PENDING("审批中"),
        APPROVED("已通过"),
        REJECTED("已驳回"),
        CANCELLED("已撤销");

        private final String name;

        InstanceStatus(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ConditionConfig {
        private Double min;
        private Double max;
        private java.util.List<String> types;
    }
}
