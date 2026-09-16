package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.workflow.entity.WfInstance;
import com.unified.common.workflow.mapper.WfInstanceMapper;
import com.unified.system.service.WfInstanceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WfInstanceServiceImpl extends ServiceImpl<WfInstanceMapper, WfInstance> implements WfInstanceService {

    @Override
    public WfInstance getByBusiness(String businessType, Long businessId) {
        return getOne(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getBusinessType, businessType)
                .eq(WfInstance::getBusinessId, businessId));
    }

    @Override
    public List<WfInstance> listByBusinessType(String businessType) {
        return list(new LambdaQueryWrapper<WfInstance>()
                .eq(WfInstance::getBusinessType, businessType)
                .orderByDesc(WfInstance::getCreateTime));
    }
}
