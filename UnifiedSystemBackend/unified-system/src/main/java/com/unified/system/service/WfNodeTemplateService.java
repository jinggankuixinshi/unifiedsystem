package com.unified.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.common.workflow.entity.WfNodeTemplate;

import java.util.List;

public interface WfNodeTemplateService extends IService<WfNodeTemplate> {

    List<WfNodeTemplate> listByTemplateId(Long templateId);
}
