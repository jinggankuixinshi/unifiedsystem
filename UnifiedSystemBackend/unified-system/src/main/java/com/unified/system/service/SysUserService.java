package com.unified.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.SysUserDTO;
import com.unified.system.entity.SysUser;
import com.unified.system.vo.SysUserVO;

import java.util.List;
import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    Map<String, Object> login(String username, String password);

    SysUser getByUsername(String username);

    SysUserVO createUser(SysUserDTO dto);

    SysUserVO updateUser(Long id, SysUserDTO dto);

    boolean deleteUser(Long id);

    IPage<SysUserVO> pageUsers(int pageNum, int pageSize, String keyword);

    SysUserVO getUserById(Long id);

    void assignRolesToUser(Long userId, List<Long> roleIds);

    List<Long> getUserRoleIds(Long userId);
}
