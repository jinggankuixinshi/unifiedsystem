package com.unified.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.AttOvertimeDTO;
import com.unified.system.entity.AttOvertime;
import com.unified.system.vo.AttOvertimeVO;

public interface AttOvertimeService extends IService<AttOvertime> {

    AttOvertimeVO apply(Long userId, AttOvertimeDTO dto);

    IPage<AttOvertimeVO> pageOvertimes(int pageNum, int pageSize, Long userId, Integer status);
}
