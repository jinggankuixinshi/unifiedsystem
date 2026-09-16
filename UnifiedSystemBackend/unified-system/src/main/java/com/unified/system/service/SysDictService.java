package com.unified.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.system.entity.SysDictData;
import com.unified.system.entity.SysDictType;
import com.unified.system.mapper.SysDictDataMapper;
import com.unified.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictService extends ServiceImpl<SysDictTypeMapper, SysDictType> {

    private final SysDictDataMapper dictDataMapper;

    public List<SysDictData> getDictDataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getSortOrder));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveDictData(SysDictData data) {
        dictDataMapper.insert(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(SysDictData data) {
        dictDataMapper.updateById(data);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        dictDataMapper.deleteById(id);
    }
}
