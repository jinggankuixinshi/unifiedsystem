package com.unified.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysRoleDTO;
import com.unified.system.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    IPage<SysRole> pageRoles(int pageNum, int pageSize, String keyword);

    SysRole createRole(SysRoleDTO dto);

    SysRole updateRole(Long id, SysRoleDTO dto);

    boolean deleteRole(Long id);

    void assignResourcesToRole(Long roleId, List<Long> resourceIds);

    void sortRoles(List<SortItemDTO> items);
}
