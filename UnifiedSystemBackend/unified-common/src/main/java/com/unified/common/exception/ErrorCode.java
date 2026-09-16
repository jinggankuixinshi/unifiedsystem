package com.unified.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "数据冲突"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),

    INVALID_TOKEN(1001, "Token无效或已过期"),
    KICKED_OUT(1002, "账号已在其他设备登录"),
    ACCOUNT_DISABLED(1003, "账号已被禁用"),

    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    PASSWORD_ERROR(2003, "原始密码错误"),
    ROLE_NOT_FOUND(2005, "角色不存在"),
    ROLE_CODE_EXISTS(2006, "角色编码已存在"),
    DEPT_NOT_FOUND(2007, "部门不存在"),
    DEPT_HAS_CHILDREN(2008, "存在子部门，无法删除"),
    RESOURCE_NOT_FOUND(2009, "资源不存在"),
    RESOURCE_HAS_CHILDREN(2010, "存在子资源，无法删除"),

    DUPLICATE_SUBMIT(3001, "请勿重复提交"),
    STOCK_INSUFFICIENT(3002, "库存不足"),
    STOCK_CHECK_FAIL(3003, "库存校验失败"),
    AMOUNT_CHECK_FAIL(3004, "金额校验失败"),

    ORDER_NOT_FOUND(4001, "单据不存在"),
    ORDER_STATUS_ERROR(4002, "单据状态不正确"),
    APPROVAL_NOT_FOUND(4005, "审批流程不存在"),
    APPROVAL_NODE_NOT_FOUND(4006, "审批节点不存在"),
    APPROVAL_NO_PERMISSION(4007, "无审批权限"),
    APPROVAL_ALREADY(4008, "已审批，请勿重复操作"),

    TRANSFER_CHECK_FAIL(5001, "调拨校验失败"),
    QUALITY_CHECK_FAIL(5002, "质检校验失败"),
    CONTRACT_SIGN_FAIL(5003, "合同签署失败"),

    ESIGN_ERROR(6001, "e签宝接口调用失败"),
    REPORT_GEN_ERROR(7001, "报表生成失败"),
    FILE_UPLOAD_ERROR(8001, "文件上传失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
