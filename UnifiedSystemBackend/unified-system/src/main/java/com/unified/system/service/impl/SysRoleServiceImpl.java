package com.unified.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysRoleDTO;
import com.unified.system.entity.SysRole;
import com.unified.system.entity.SysRoleResource;
import com.unified.system.entity.SysUserRole;
import com.unified.system.mapper.SysRoleMapper;
import com.unified.system.mapper.SysRoleResourceMapper;
import com.unified.system.mapper.SysUserRoleMapper;
import com.unified.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleResourceMapper roleResourceMapper;

    @Override
    public IPage<SysRole> pageRoles(int pageNum, int pageSize, String keyword) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;

        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SysRole::getRoleName, keyword)
                   .or().like(SysRole::getRoleCode, keyword);
        }
        wrapper.orderByAsc(SysRole::getSortOrder)
               .orderByAsc(SysRole::getId);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole createRole(SysRoleDTO dto) {
        if (isRoleCodeExist(dto.getRoleCode(), null)) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        SysRole entity = new SysRole();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        entity.setSortOrder(nextSortOrder());
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(Long id, SysRoleDTO dto) {
        SysRole entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        if (!entity.getRoleCode().equals(dto.getRoleCode()) && isRoleCodeExist(dto.getRoleCode(), id)) {
            throw new BusinessException(ErrorCode.ROLE_CODE_EXISTS);
        }
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        roleResourceMapper.delete(new LambdaQueryWrapper<SysRoleResource>().eq(SysRoleResource::getRoleId, id));
        return removeById(id);
    }

    private boolean isRoleCodeExist(String roleCode, Long excludeId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode);
        if (excludeId != null) {
            wrapper.ne(SysRole::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortRoles(List<SortItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "排序数据不能为空");
        }
        for (SortItemDTO item : items) {
            if (item.getId() == null || item.getSortOrder() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "排序数据不完整");
            }
        }
        List<Long> ids = items.stream().map(SortItemDTO::getId).collect(java.util.stream.Collectors.toList());
        if (listByIds(ids).size() != ids.size()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        for (SortItemDTO item : items) {
            update(new LambdaUpdateWrapper<SysRole>()
                    .eq(SysRole::getId, item.getId())
                    .set(SysRole::getSortOrder, item.getSortOrder()));
        }
    }

    private int nextSortOrder() {
        List<SysRole> roles = list(new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getSortOrder)
                .orderByDesc(SysRole::getId));
        Integer max = roles.isEmpty() ? null : roles.get(0).getSortOrder();
        return max == null ? 1 : max + 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignResourcesToRole(Long roleId, List<Long> resourceIds) {
        roleResourceMapper.delete(new LambdaQueryWrapper<SysRoleResource>().eq(SysRoleResource::getRoleId, roleId));
        if (resourceIds != null && !resourceIds.isEmpty()) {
            List<SysRoleResource> roleResources = resourceIds.stream().map(resourceId -> {
                SysRoleResource rr = new SysRoleResource();
                rr.setRoleId(roleId);
                rr.setResourceId(resourceId);
                return rr;
            }).collect(java.util.stream.Collectors.toList());
            for (SysRoleResource rr : roleResources) {
                roleResourceMapper.insert(rr);
            }
        }
    }
}
