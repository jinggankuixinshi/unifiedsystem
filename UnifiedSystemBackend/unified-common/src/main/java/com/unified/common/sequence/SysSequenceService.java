package com.unified.common.sequence;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 业务序列号：基于 sys_sequence 表按「前缀 + 日期」原子递增，重启/多实例不重号
 */
@Service
@RequiredArgsConstructor
public class SysSequenceService {

    private final SysSequenceMapper sequenceMapper;

    @DSTransactional(rollbackFor = Exception.class)
    public int nextSeq(String prefix, LocalDate date) {
        SysSequence row = sequenceMapper.selectForUpdate(prefix, date);
        if (row == null) {
            row = new SysSequence();
            row.setPrefix(prefix);
            row.setCurrentSeq(1);
            row.setUpdateDate(date);
            try {
                sequenceMapper.insert(row);
                return 1;
            } catch (DuplicateKeyException e) {
                // 并发首次创建：转为行锁更新
                row = sequenceMapper.selectForUpdate(prefix, date);
            }
        }
        int seq = (row.getCurrentSeq() == null ? 0 : row.getCurrentSeq()) + 1;
        row.setCurrentSeq(seq);
        sequenceMapper.updateById(row);
        return seq;
    }
}
