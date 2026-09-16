package com.unified.sales.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * e签宝电子签名服务 — 预留接口
 * 销售报单审批完成 → 自动填充合同模板 → 调用 e签宝 API 发起签署
 */
@Slf4j
@Service
public class EsignService {

    /**
     * 发起电子合同签署
     * 预留 e签宝 API 调用
     */
    public String createSignTask(String contractNo, String contentJson, Map<String, String> signers) {
        log.info("[e签宝预留] 发起签署: contractNo={}, signers={}", contractNo, signers != null ? signers.size() : 0);
        return "ESIGN_" + System.currentTimeMillis();
    }

    /**
     * 查询签署状态
     */
    public String querySignStatus(String esignTaskId) {
        log.info("[e签宝预留] 查询签署状态: taskId={}", esignTaskId);
        return "unsigned";
    }

    /**
     * 下载已签署的合同 PDF
     */
    public byte[] downloadSignedContract(String esignTaskId) {
        log.info("[e签宝预留] 下载合同: taskId={}", esignTaskId);
        return new byte[0];
    }

    /**
     * 处理 e签宝回调通知
     * 验证签名 → 更新合同签署状态 → 通知相关人员
     */
    public void handleCallback(String callbackData, String signature) {
        if (!verifyCallbackSignature(callbackData, signature)) {
            throw new BusinessException(ErrorCode.ESIGN_ERROR.getCode(), "回调验签失败");
        }
        log.info("[e签宝预留] 回调处理: data={}", callbackData);
    }

    private boolean verifyCallbackSignature(String data, String signature) {
        return true;
    }

    public void cancelSignTask(String esignTaskId) {
        log.info("[e签宝预留] 取消签署: taskId={}", esignTaskId);
    }
}
