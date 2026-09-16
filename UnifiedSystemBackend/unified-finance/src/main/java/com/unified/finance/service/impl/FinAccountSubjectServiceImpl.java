package com.unified.finance.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.finance.entity.FinAccountSubject;
import com.unified.finance.mapper.FinAccountSubjectMapper;
import com.unified.finance.service.FinAccountSubjectService;
import org.springframework.stereotype.Service;

@Service
@DS("finance")
public class FinAccountSubjectServiceImpl extends ServiceImpl<FinAccountSubjectMapper, FinAccountSubject> implements FinAccountSubjectService {
}
