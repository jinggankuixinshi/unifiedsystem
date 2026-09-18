package com.unified.finance.dto;

import com.unified.finance.entity.FinExpense;
import com.unified.finance.entity.FinExpenseItem;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExpenseCreateDTO {

    @NotNull(message = "报销信息不能为空")
    private FinExpense expense;

    private List<FinExpenseItem> items;
}
