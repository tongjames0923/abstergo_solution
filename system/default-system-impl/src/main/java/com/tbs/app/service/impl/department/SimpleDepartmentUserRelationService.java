package com.tbs.app.service.impl.department;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.department.ISystemDepartmentService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.mapper.SystemUserDepartmentRelationMapper;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.shared.pojo.entity.SystemUserDepartmentRelationEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.dsl.clause.JoinCondition;
import com.tbs.sql.service.QueryEngine;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static com.tbs.app.service.impl.CacheNamesConstants.USER_DEPARTMENTS;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import com.tbs.app.constant.SystemI18nConstant;

public class SimpleDepartmentUserRelationService implements IDepartmentUserRelationService {

    @Resource
    private SystemUserDepartmentRelationMapper departmentRelationMapper;

    @Resource
    private ISystemDepartmentService departmentService;

    @Resource
    private ISystemUserService userService;

    @Cacheable(cacheNames = USER_DEPARTMENTS, key = "#userId")
    @ExpireOn(value = 3, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<SystemDepartmentEntity> queryUserDepartments( Long userId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_FOUND).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        return QueryEngine.getEntityQueryWrapper(new SystemDepartmentEntity()).query().where().subQuery().exists(
            QueryEngine.getEntityQueryWrapper(new SystemUserDepartmentRelationEntity()).query().where()
                .equal(SystemUserDepartmentRelationEntity::getUserId, userId).pour(),
                JoinCondition.of(SystemUserDepartmentRelationEntity::getDepartmentId, SystemDepartmentEntity::getId)).pour()
            .fetch();
    }

    @Override
    public List<SystemUserEntity> queryDepartmentUsers( Long departmentId) {
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.isTrue(department == null).i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_NOT_FOUND).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        return departmentRelationMapper.queryDepartmentUsers(departmentId);
    }

    @CacheEvict(cacheNames = USER_DEPARTMENTS, key = "#userId")
    @Override
    public void addUserToDepartment( Long userId,  Long departmentId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_FOUND).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.isTrue(department == null).i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_NOT_FOUND).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        var relations = departmentRelationMapper.queryUserDepartmentRelation(userId, departmentId);
        AssertUtil.isTrue(CollUtil.isNotEmpty(relations)).i18n(com.tbs.app.constant.SystemI18nConstant.USER_ALREADY_IN_DEPARTMENT)
            .params(department.getDepartmentName())
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        SystemUserDepartmentRelationEntity relation = new SystemUserDepartmentRelationEntity();
        relation.setUserId(userId);
        relation.setDepartmentId(departmentId);
        departmentRelationMapper.insert(relation);
    }

    @CacheEvict(cacheNames = USER_DEPARTMENTS, key = "#userId")
    @Override
    public void deleteUserFromDepartment( Long userId,  Long departmentId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.isTrue(user == null).i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_FOUND).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        SystemDepartmentEntity department = departmentService.queryById(departmentId);
        AssertUtil.isTrue(department == null).i18n(com.tbs.app.constant.SystemI18nConstant.DEPARTMENT_NOT_FOUND).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        var relations = departmentRelationMapper.queryUserDepartmentRelation(userId, departmentId);
        AssertUtil.isTrue(CollUtil.isEmpty(relations)).i18n(com.tbs.app.constant.SystemI18nConstant.USER_NOT_IN_DEPARTMENT)
            .params(department.getDepartmentName())
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        departmentRelationMapper.deleteByPrimaryKey(relations.get(0).getId());
    }
}
