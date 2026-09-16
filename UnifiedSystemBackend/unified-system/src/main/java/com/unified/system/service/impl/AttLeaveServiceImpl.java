package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.system.dto.AttLeaveDTO;
import com.unified.system.entity.AttLeave;
import com.unified.system.mapper.AttLeaveMapper;
import com.unified.system.service.AttLeaveService;
import com.unified.system.vo.AttLeaveVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttLeaveServiceImpl extends ServiceImpl<AttLeaveMapper, AttLeave> implements AttLeaveService {

    @Override
    public AttLeaveVO apply(Long userId, AttLeaveDTO dto) {
        if (dto.getEndTime() != null && dto.getStartTime() != null
                && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "结束时间不能早于开始时间");
        }

        AttLeave entity = new AttLeave();
        BeanUtils.copyProperties(dto, entity);
        entity.setUserId(userId);
        entity.setApprovalStatus(0);
        save(entity);

        log.info("请假申请已提交: userId={}, type={}, duration={}", userId, dto.getLeaveType(), dto.getDuration());
        return toVO(entity);
    }

    @Override
    public IPage<AttLeaveVO> pageLeaves(int pageNum, int pageSize, Long userId, Integer status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;

        LambdaQueryWrapper<AttLeave> wrapper = new LambdaQueryWrapper<AttLeave>()
                .eq(userId != null, AttLeave::getUserId, userId)
                .eq(status != null, AttLeave::getApprovalStatus, status)
                .orderByDesc(AttLeave::getCreateTime);

        IPage<AttLeave> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<AttLeaveVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());

        IPage<AttLeaveVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    private AttLeaveVO toVO(AttLeave entity) {
        AttLeaveVO vo = new AttLeaveVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
