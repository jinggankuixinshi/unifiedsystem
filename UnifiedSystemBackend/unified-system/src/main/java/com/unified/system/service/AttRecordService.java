package com.unified.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.entity.AttRecord;
import com.unified.system.vo.AttRecordVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

public interface AttRecordService extends IService<AttRecord> {

    void clockIn(Long userId);

    void clockOut(Long userId);

    IPage<AttRecordVO> pageRecords(int pageNum, int pageSize, Long userId);
}
