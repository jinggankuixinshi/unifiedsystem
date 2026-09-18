package com.unified.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.HrAccountOpDTO;
import com.unified.system.entity.HrAccountOp;

public interface HrAccountOpService extends IService<HrAccountOp> {

    HrAccountOp submit(HrAccountOpDTO dto, Long applicantId);

    IPage<HrAccountOp> pageOps(int pageNum, int pageSize, Integer status);

    void execute(HrAccountOp op);
}
