package com.unified.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.workflow.entity.WfTemplate;
import com.unified.common.workflow.mapper.WfTemplateMapper;
import com.unified.system.service.WfTemplateService;
import org.springframework.stereotype.Service;

@Service
public class WfTemplateServiceImpl extends ServiceImpl<WfTemplateMapper, WfTemplate> implements WfTemplateService {
}
