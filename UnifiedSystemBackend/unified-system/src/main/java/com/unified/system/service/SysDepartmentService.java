package com.unified.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysDepartmentDTO;
import com.unified.system.entity.SysDepartment;

import java.util.List;

public interface SysDepartmentService extends IService<SysDepartment> {

    List<SysDepartment> getDeptTree();

    SysDepartment createDept(SysDepartmentDTO dto);

    SysDepartment updateDept(Long id, SysDepartmentDTO dto);

    boolean deleteDept(Long id);

    void sortDepts(List<SortItemDTO> items);
}
