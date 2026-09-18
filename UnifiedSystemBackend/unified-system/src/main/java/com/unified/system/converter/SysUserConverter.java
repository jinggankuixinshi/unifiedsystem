package com.unified.system.converter;

import com.unified.system.dto.SysUserDTO;
import com.unified.system.entity.SysUser;
import com.unified.system.vo.SysUserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 用户对象映射（MapStruct 编译期生成实现，替代手写 BeanUtils.copyProperties）
 */
@Mapper(componentModel = "spring")
public interface SysUserConverter {

    @Mapping(target = "password", ignore = true)
    SysUser toEntity(SysUserDTO dto);

    @Mapping(target = "password", ignore = true)
    void updateEntity(SysUserDTO dto, @MappingTarget SysUser entity);

    @Mapping(target = "phone", ignore = true)
    @Mapping(target = "deptName", ignore = true)
    SysUserVO toVO(SysUser entity);
}
