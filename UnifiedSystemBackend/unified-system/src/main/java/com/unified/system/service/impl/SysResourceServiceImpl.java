package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysResourceDTO;
import com.unified.system.entity.SysResource;
import com.unified.system.mapper.SysResourceMapper;
import com.unified.system.service.SysResourceService;
import com.unified.system.vo.SysResourceVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysResourceServiceImpl extends ServiceImpl<SysResourceMapper, SysResource> implements SysResourceService {

    @Override
    public List<SysResourceVO> getResourceTree() {
        List<SysResource> all = list(new LambdaQueryWrapper<SysResource>()
                .orderByAsc(SysResource::getSortOrder)
                .orderByAsc(SysResource::getId));
        List<SysResourceVO> voList = all.stream().map(this::toVO).collect(Collectors.toList());
        Map<Long, List<SysResourceVO>> parentMap = voList.stream()
                .filter(r -> r.getParentId() != null && r.getParentId() != 0)
                .collect(Collectors.groupingBy(SysResourceVO::getParentId));
        List<SysResourceVO> roots = new ArrayList<>();
        for (SysResourceVO vo : voList) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                buildChildren(vo, parentMap);
                roots.add(vo);
            }
        }
        return roots;
    }

    private void buildChildren(SysResourceVO parent, Map<Long, List<SysResourceVO>> parentMap) {
        List<SysResourceVO> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (SysResourceVO child : children) {
                buildChildren(child, parentMap);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysResource createResource(SysResourceDTO dto) {
        SysResource entity = new SysResource();
        BeanUtils.copyProperties(dto, entity);
        if (entity.getParentId() == null) {
            entity.setParentId(0L);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        entity.setSortOrder(nextSortOrder(entity.getParentId()));
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysResource updateResource(Long id, SysResourceDTO dto) {
        SysResource entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        Long oldParentId = entity.getParentId() == null ? 0L : entity.getParentId();
        Long newParentId = dto.getParentId() == null ? 0L : dto.getParentId();
        BeanUtils.copyProperties(dto, entity);
        entity.setParentId(newParentId);
        if (!oldParentId.equals(newParentId)) {
            entity.setSortOrder(nextSortOrder(newParentId));
        }
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResource(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        long childCount = count(new LambdaQueryWrapper<SysResource>().eq(SysResource::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.RESOURCE_HAS_CHILDREN);
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortResources(List<SortItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "排序数据不能为空");
        }
        for (SortItemDTO item : items) {
            if (item.getId() == null || item.getSortOrder() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "排序数据不完整");
            }
        }
        List<Long> ids = items.stream().map(SortItemDTO::getId).collect(Collectors.toList());
        List<SysResource> resources = listByIds(ids);
        if (resources.size() != ids.size()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        Set<Long> parentIds = resources.stream()
                .map(r -> r.getParentId() == null ? 0L : r.getParentId())
                .collect(Collectors.toSet());
        if (parentIds.size() > 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "仅支持同级资源排序");
        }
        for (SortItemDTO item : items) {
            update(new LambdaUpdateWrapper<SysResource>()
                    .eq(SysResource::getId, item.getId())
                    .set(SysResource::getSortOrder, item.getSortOrder()));
        }
    }

    private int nextSortOrder(Long parentId) {
        List<SysResource> siblings = list(new LambdaQueryWrapper<SysResource>()
                .eq(SysResource::getParentId, parentId)
                .orderByDesc(SysResource::getSortOrder)
                .orderByDesc(SysResource::getId));
        Integer max = siblings.isEmpty() ? null : siblings.get(0).getSortOrder();
        return max == null ? 1 : max + 1;
    }

    private SysResourceVO toVO(SysResource entity) {
        SysResourceVO vo = new SysResourceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
