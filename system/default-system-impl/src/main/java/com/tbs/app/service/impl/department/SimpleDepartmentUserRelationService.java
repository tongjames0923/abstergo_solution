package com.tbs.app.service.impl.department;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.mapper.SystemUserDepartmentRelationMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemUserDepartmentRelationEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SimpleDepartmentUserRelationService implements IDepartmentUserRelationService {

    @Resource
    private SystemUserDepartmentRelationMapper departmentRelationMapper;

    @Resource
    private ISystemDepartmentService departmentService;

    @Resource
    private ISystemUserService userService;

    @Cacheable(cacheNames = "user_departments", key = "#userId")
    @ExpireOn(value = 3, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<SystemDepartmentEntity> queryUserDepartments(@NotNull Long userId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        return departmentRelationMapper.queryUserDepartments(userId);
    }

    @Override
    public List<SystemUserEntity> queryDepartmentUsers(@NotNull Long departmentId) {
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.notNull(department, "部门不存在");
        return departmentRelationMapper.queryDepartmentUsers(departmentId);
    }

    @CacheEvict(cacheNames = "user_departments", key = "#userId")
    @Override
    public void addUserToDepartment(@NotNull Long userId, @NotNull Long departmentId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.notNull(department, "部门不存在");
        var relations = departmentRelationMapper.queryUserDepartmentRelation(userId, departmentId);
        AssertUtil.isTrue(!CollUtil.isEmpty(relations), "用户已加入 {} ", department.getDepartmentName());
        SystemUserDepartmentRelationEntity relation = new SystemUserDepartmentRelationEntity();
        relation.setUserId(userId);
        relation.setDepartmentId(departmentId);
        departmentRelationMapper.insert(relation);
    }

    @CacheEvict(cacheNames = "user_departments", key = "#userId")
    @Override
    public void deleteUserFromDepartment(@NotNull Long userId, @NotNull Long departmentId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.notNull(department, "部门不存在");
        var relations = departmentRelationMapper.queryUserDepartmentRelation(userId, departmentId);
        AssertUtil.isTrue(!CollUtil.isNotEmpty(relations), "用户未加入 {}", department.getDepartmentName());
        departmentRelationMapper.deleteByPrimaryKey(relations.get(0).getId());
    }
}
