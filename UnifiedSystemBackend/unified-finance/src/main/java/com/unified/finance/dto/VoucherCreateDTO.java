package com.unified.finance.dto;

import com.unified.finance.entity.FinVoucherEntry;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VoucherCreateDTO {

    private LocalDate voucherDate;
    private String summary;
    private List<FinVoucherEntry> entries;
}
