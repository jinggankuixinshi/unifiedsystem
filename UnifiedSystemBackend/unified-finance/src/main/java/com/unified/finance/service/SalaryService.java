package com.unified.finance.service;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.finance.entity.FinSalary;
import com.unified.finance.entity.FinSalaryItem;
import com.unified.finance.mapper.FinSalaryItemMapper;
import com.unified.finance.mapper.FinSalaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 工资管理服务
 * netSalary = baseSalary + performanceBonus + overtimePay - deduction - socialInsurance - housingFund
 */
@Service
@RequiredArgsConstructor
public class SalaryService extends ServiceImpl<FinSalaryMapper, FinSalary> {

    private final FinSalaryItemMapper itemMapper;

    @Transactional(rollbackFor = Exception.class)
    public FinSalary calculateSalary(FinSalary salary) {
        BigDecimal net = BigDecimal.ZERO
                .add(salary.getBaseSalary() != null ? salary.getBaseSalary() : BigDecimal.ZERO)
                .add(salary.getPerformanceBonus() != null ? salary.getPerformanceBonus() : BigDecimal.ZERO)
                .add(salary.getOvertimePay() != null ? salary.getOvertimePay() : BigDecimal.ZERO)
                .subtract(salary.getDeduction() != null ? salary.getDeduction() : BigDecimal.ZERO)
                .subtract(salary.getSocialInsurance() != null ? salary.getSocialInsurance() : BigDecimal.ZERO)
                .subtract(salary.getHousingFund() != null ? salary.getHousingFund() : BigDecimal.ZERO);

        salary.setNetSalary(net.setScale(2, RoundingMode.HALF_UP));
        salary.setStatus(0);

        if (salary.getId() != null) {
            updateById(salary);
        } else {
            save(salary);
        }
        return salary;
    }

    public void saveItems(Long salaryId, List<FinSalaryItem> items) {
        for (FinSalaryItem item : items) {
            item.setSalaryId(salaryId);
            itemMapper.insert(item);
        }
    }
}
