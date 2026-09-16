package com.unified.finance.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unified.finance.entity.FinAccountSubject;
import org.apache.ibatis.annotations.Mapper;

@DS("finance")
@Mapper
public interface FinAccountSubjectMapper extends BaseMapper<FinAccountSubject> {
}
