package com.unified.common.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.common.workflow.entity.WfInstance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WfInstanceMapper extends BaseMapper<WfInstance> {
}
