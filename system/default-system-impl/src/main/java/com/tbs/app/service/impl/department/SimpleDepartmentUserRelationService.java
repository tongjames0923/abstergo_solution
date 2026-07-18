package com.tbs.app.service.impl.department;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.mapper.SystemUserDepartmentRelationMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemUserDepartmentRelationEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.dsl.clause.JoinCondition;
import com.tbs.sql.service.QueryEngine;
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
        AssertUtil.isTrue(user == null).message(I18nUtil.get("user.not.found")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        return QueryEngine.getEntityQueryWrapper(new SystemDepartmentEntity()).query().where().subQuery().exists(
            QueryEngine.getEntityQueryWrapper(new SystemUserDepartmentRelationEntity()).query().where()
                .equal(SystemUserDepartmentRelationEntity::getUserId, userId).pour(),
                JoinCondition.of(SystemUserDepartmentRelationEntity::getDepartmentId, SystemDepartmentEntity::getId)).pour()
            .fetch();
    }

    @Override
    public List<SystemUserEntity> queryDepartmentUsers(@NotNull Long departmentId) {
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.isTrue(department == null).message(I18nUtil.get("department.not.found")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return departmentRelationMapper.queryDepartmentUsers(departmentId);
    }

    @CacheEvict(cacheNames = "user_departments", key = "#userId")
    @Override
    public void addUserToDepartment(@NotNull Long userId, @NotNull Long departmentId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).message(I18nUtil.get("user.not.found")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.isTrue(department == null).message(I18nUtil.get("department.not.found")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        var relations = departmentRelationMapper.queryUserDepartmentRelation(userId, departmentId);
        AssertUtil.isTrue(CollUtil.isNotEmpty(relations)).message(I18nUtil.get("user.already.in.department", department.getDepartmentName()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        SystemUserDepartmentRelationEntity relation = new SystemUserDepartmentRelationEntity();
        relation.setUserId(userId);
        relation.setDepartmentId(departmentId);
        departmentRelationMapper.insert(relation);
    }

    @CacheEvict(cacheNames = "user_departments", key = "#userId")
    @Override
    public void deleteUserFromDepartment(@NotNull Long userId, @NotNull Long departmentId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).message(I18nUtil.get("user.not.found")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.isTrue(department == null).message(I18nUtil.get("department.not.found")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        var relations = departmentRelationMapper.queryUserDepartmentRelation(userId, departmentId);
        AssertUtil.isTrue(CollUtil.isEmpty(relations)).message(I18nUtil.get("user.not.in.department", department.getDepartmentName()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        departmentRelationMapper.deleteByPrimaryKey(relations.get(0).getId());
    }
}