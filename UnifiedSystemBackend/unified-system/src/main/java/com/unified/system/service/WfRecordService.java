package com.unified.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.common.workflow.entity.WfRecord;

import java.util.List;

public interface WfRecordService extends IService<WfRecord> {

    List<WfRecord> listByInstanceId(Long instanceId);
}
