package com.unified.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysDepartmentDTO;
import com.unified.system.entity.SysDepartment;
import com.unified.system.mapper.SysDepartmentMapper;
import com.unified.system.service.SysDepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements SysDepartmentService {

    @Override
    public List<SysDepartment> getDeptTree() {
        List<SysDepartment> all = list(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getSortOrder)
                .orderByAsc(SysDepartment::getId));
        Map<Long, List<SysDepartment>> parentMap = all.stream()
                .filter(d -> d.getParentId() != null && d.getParentId() != 0)
                .collect(Collectors.groupingBy(SysDepartment::getParentId));
        List<SysDepartment> roots = new ArrayList<>();
        for (SysDepartment dept : all) {
            if (dept.getParentId() == null || dept.getParentId() == 0) {
                buildChildren(dept, parentMap);
                roots.add(dept);
            }
        }
        return roots;
    }

    private void buildChildren(SysDepartment parent, Map<Long, List<SysDepartment>> parentMap) {
        List<SysDepartment> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            for (SysDepartment child : children) {
                buildChildren(child, parentMap);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDepartment createDept(SysDepartmentDTO dto) {
        SysDepartment entity = new SysDepartment();
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
    public SysDepartment updateDept(Long id, SysDepartmentDTO dto) {
        SysDepartment entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
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
    public boolean deleteDept(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
        }
        long childCount = count(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.DEPT_HAS_CHILDREN);
        }
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sortDepts(List<SortItemDTO> items) {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "排序数据不能为空");
        }
        for (SortItemDTO item : items) {
            if (item.getId() == null || item.getSortOrder() == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "排序数据不完整");
            }
        }
        List<Long> ids = items.stream().map(SortItemDTO::getId).collect(Collectors.toList());
        List<SysDepartment> depts = listByIds(ids);
        if (depts.size() != ids.size()) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
        }
        Set<Long> parentIds = depts.stream()
                .map(d -> d.getParentId() == null ? 0L : d.getParentId())
                .collect(Collectors.toSet());
        if (parentIds.size() > 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "仅支持同级部门排序");
        }
        for (SortItemDTO item : items) {
            update(new LambdaUpdateWrapper<SysDepartment>()
                    .eq(SysDepartment::getId, item.getId())
                    .set(SysDepartment::getSortOrder, item.getSortOrder()));
        }
    }

    private int nextSortOrder(Long parentId) {
        List<SysDepartment> siblings = list(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getParentId, parentId)
                .orderByDesc(SysDepartment::getSortOrder)
                .orderByDesc(SysDepartment::getId));
        Integer max = siblings.isEmpty() ? null : siblings.get(0).getSortOrder();
        return max == null ? 1 : max + 1;
    }
}
