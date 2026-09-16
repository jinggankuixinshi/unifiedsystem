package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.system.entity.AttRecord;
import com.unified.system.mapper.AttRecordMapper;
import com.unified.system.service.AttRecordService;
import com.unified.system.vo.AttRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttRecordServiceImpl extends ServiceImpl<AttRecordMapper, AttRecord> implements AttRecordService {

    @Override
    public void clockIn(Long userId) {
        AttRecord record = new AttRecord();
        record.setUserId(userId);
        record.setLoginTime(LocalDateTime.now());
        save(record);
        log.info("考勤打卡: userId={}", userId);
    }

    @Override
    public void clockOut(Long userId) {
        AttRecord record = getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttRecord>()
                .eq(AttRecord::getUserId, userId)
                .isNull(AttRecord::getLogoutTime)
                .orderByDesc(AttRecord::getLoginTime));
        if (record != null) {
            record.setLogoutTime(LocalDateTime.now());
            updateById(record);
            log.info("考勤签退: userId={}", userId);
        }
    }

    @Override
    public IPage<AttRecordVO> pageRecords(int pageNum, int pageSize, Long userId) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttRecord> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttRecord>()
                        .eq(userId != null, AttRecord::getUserId, userId)
                        .orderByDesc(AttRecord::getLoginTime);

        IPage<AttRecord> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<AttRecordVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());

        IPage<AttRecordVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    private AttRecordVO toVO(AttRecord entity) {
        AttRecordVO vo = new AttRecordVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
