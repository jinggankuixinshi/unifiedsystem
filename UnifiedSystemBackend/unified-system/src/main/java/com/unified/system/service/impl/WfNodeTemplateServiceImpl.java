package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.workflow.entity.WfNodeTemplate;
import com.unified.common.workflow.mapper.WfNodeTemplateMapper;
import com.unified.system.service.WfNodeTemplateService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WfNodeTemplateServiceImpl extends ServiceImpl<WfNodeTemplateMapper, WfNodeTemplate> implements WfNodeTemplateService {

    @Override
    public List<WfNodeTemplate> listByTemplateId(Long templateId) {
        return list(new LambdaQueryWrapper<WfNodeTemplate>()
                .eq(WfNodeTemplate::getTemplateId, templateId)
                .orderByAsc(WfNodeTemplate::getNodeOrder));
    }
}
