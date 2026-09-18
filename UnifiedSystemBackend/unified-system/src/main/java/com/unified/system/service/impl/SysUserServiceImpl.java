package com.unified.system.service.impl;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unified.common.exception.BusinessException;
import com.unified.common.exception.ErrorCode;
import com.unified.common.security.JwtUtil;
import com.unified.common.security.TokenManager;
import com.unified.system.converter.SysUserConverter;
import com.unified.system.dto.SysUserDTO;
import com.unified.system.entity.SysDepartment;
import com.unified.system.entity.SysUser;
import com.unified.system.entity.SysUserRole;
import com.unified.system.mapper.SysDepartmentMapper;
import com.unified.system.mapper.SysUserMapper;
import com.unified.system.mapper.SysUserRoleMapper;
import com.unified.system.service.SysUserService;
import com.unified.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final JwtUtil jwtUtil;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper userRoleMapper;
    private final SysDepartmentMapper departmentMapper;
    private final SysUserConverter userConverter;

    @Override
    public Map<String, Object> login(String username, String password) {
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND.getCode(), "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), new HashMap<>());
        tokenManager.updateLatestToken(user.getId(), token);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", Map.of(
                "userId", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName(),
                "deptId", user.getDeptId()
        ));
        return result;
    }

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserVO createUser(SysUserDTO dto) {
        SysUser exist = getByUsername(dto.getUsername());
        if (exist != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        SysUser entity = userConverter.toEntity(dto);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        save(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUserVO updateUser(Long id, SysUserDTO dto) {
        SysUser entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!entity.getUsername().equals(dto.getUsername())) {
            SysUser exist = getByUsername(dto.getUsername());
            if (exist != null) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS);
            }
        }
        userConverter.updateEntity(dto, entity);
        if (StrUtil.isNotBlank(dto.getPassword())) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        updateById(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        SysUser entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        return removeById(id);
    }

    @Override
    public IPage<SysUserVO> pageUsers(int pageNum, int pageSize, String keyword) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize > 200) pageSize = 200;

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(SysUser::getUsername, keyword)
                   .or().like(SysUser::getRealName, keyword)
                   .or().like(SysUser::getPhone, keyword);
        }
        wrapper.orderByAsc(SysUser::getId);

        IPage<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<SysUserVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        IPage<SysUserVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public SysUserVO getUserById(Long id) {
        SysUser entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> userRoles = roleIds.stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            }).collect(Collectors.toList());
            for (SysUserRole ur : userRoles) {
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
    }

    private SysUserVO toVO(SysUser entity) {
        SysUserVO vo = userConverter.toVO(entity);
        if (StrUtil.isNotBlank(entity.getPhone())) {
            vo.setPhone(maskPhone(entity.getPhone()));
        }
        if (entity.getDeptId() != null) {
            SysDepartment dept = departmentMapper.selectById(entity.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }
        return vo;
    }

    private String maskPhone(String phone) {
        if (phone.length() == 11) {
            return DesensitizedUtil.mobilePhone(phone);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
