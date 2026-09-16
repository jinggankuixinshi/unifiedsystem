package com.unified.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.common.workflow.entity.WfInstance;

import java.util.List;

public interface WfInstanceService extends IService<WfInstance> {

    WfInstance getByBusiness(String businessType, Long businessId);

    List<WfInstance> listByBusinessType(String businessType);
}
