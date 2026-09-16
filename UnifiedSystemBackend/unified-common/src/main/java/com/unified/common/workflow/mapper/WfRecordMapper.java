package com.unified.common.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.common.workflow.entity.WfRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfRecordMapper extends BaseMapper<WfRecord> {
}
