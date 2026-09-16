package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.workflow.entity.WfRecord;
import com.unified.common.workflow.mapper.WfRecordMapper;
import com.unified.system.service.WfRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WfRecordServiceImpl extends ServiceImpl<WfRecordMapper, WfRecord> implements WfRecordService {

    @Override
    public List<WfRecord> listByInstanceId(Long instanceId) {
        return list(new LambdaQueryWrapper<WfRecord>()
                .eq(WfRecord::getInstanceId, instanceId)
                .orderByAsc(WfRecord::getNodeOrder));
    }
}
