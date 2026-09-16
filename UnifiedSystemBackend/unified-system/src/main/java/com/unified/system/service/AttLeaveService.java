package com.unified.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.AttLeaveDTO;
import com.unified.system.entity.AttLeave;
import com.unified.system.vo.AttLeaveVO;

public interface AttLeaveService extends IService<AttLeave> {

    AttLeaveVO apply(Long userId, AttLeaveDTO dto);

    IPage<AttLeaveVO> pageLeaves(int pageNum, int pageSize, Long userId, Integer status);
}
