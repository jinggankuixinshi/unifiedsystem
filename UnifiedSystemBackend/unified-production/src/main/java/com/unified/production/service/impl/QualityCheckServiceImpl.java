package com.unified.production.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.constant.QualityCheckResultEnum;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.UserContext;
import com.unified.production.entity.ProdQualityCheck;
import com.unified.production.mapper.ProdQualityCheckMapper;
import com.unified.production.service.QualityCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@DS("production")
public class QualityCheckServiceImpl extends ServiceImpl<ProdQualityCheckMapper, ProdQualityCheck> implements QualityCheckService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProdQualityCheck performCheck(ProdQualityCheck check) {
        if (check.getSampleQuantity() == null || check.getUnqualifiedQuantity() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "抽样数量和不合格数量不能为空");
        }
        if (check.getSampleQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "抽样数量必须大于0");
        }
        if (check.getUnqualifiedQuantity().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不合格数量不能为负数");
        }
        if (check.getUnqualifiedQuantity().compareTo(check.getSampleQuantity()) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不合格数量不能超过抽样数量");
        }

        UserContext ctx = UserContext.get();
        if (ctx != null) {
            check.setCheckerId(ctx.getUserId());
        }
        check.setCheckTime(LocalDateTime.now());

        BigDecimal qualified = check.getSampleQuantity().subtract(check.getUnqualifiedQuantity());
        check.setQualifiedQuantity(qualified);
        check.setResult(check.getUnqualifiedQuantity().compareTo(BigDecimal.ZERO) > 0
                ? QualityCheckResultEnum.UNQUALIFIED.getLabel()
                : QualityCheckResultEnum.QUALIFIED.getLabel());

        save(check);
        log.info("质检完成: type={}, result={}, qualified={}/{}",
                check.getCheckType(), check.getResult(), qualified, check.getSampleQuantity());
        return check;
    }

    @Override
    public IPage<ProdQualityCheck> pageChecks(int pageNum, int pageSize, String checkType) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;
        LambdaQueryWrapper<ProdQualityCheck> wrapper = new LambdaQueryWrapper<ProdQualityCheck>()
                .eq(checkType != null, ProdQualityCheck::getCheckType, checkType)
                .orderByDesc(ProdQualityCheck::getCheckTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }
}
