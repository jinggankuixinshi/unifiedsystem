package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.system.dto.AttOvertimeDTO;
import com.unified.system.entity.AttOvertime;
import com.unified.system.mapper.AttOvertimeMapper;
import com.unified.system.service.AttOvertimeService;
import com.unified.system.vo.AttOvertimeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttOvertimeServiceImpl extends ServiceImpl<AttOvertimeMapper, AttOvertime> implements AttOvertimeService {

    @Override
    public AttOvertimeVO apply(Long userId, AttOvertimeDTO dto) {
        if (dto.getEndTime() != null && dto.getStartTime() != null
                && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "结束时间不能早于开始时间");
        }

        AttOvertime entity = new AttOvertime();
        BeanUtils.copyProperties(dto, entity);
        entity.setUserId(userId);
        entity.setApprovalStatus(0);
        save(entity);

        log.info("加班申请已提交: userId={}, duration={}", userId, dto.getDuration());
        return toVO(entity);
    }

    @Override
    public IPage<AttOvertimeVO> pageOvertimes(int pageNum, int pageSize, Long userId, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;

        LambdaQueryWrapper<AttOvertime> wrapper = new LambdaQueryWrapper<AttOvertime>()
                .eq(userId != null, AttOvertime::getUserId, userId)
                .eq(status != null, AttOvertime::getApprovalStatus, status)
                .orderByDesc(AttOvertime::getCreateTime);

        IPage<AttOvertime> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<AttOvertimeVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());

        IPage<AttOvertimeVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    private AttOvertimeVO toVO(AttOvertime entity) {
        AttOvertimeVO vo = new AttOvertimeVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
