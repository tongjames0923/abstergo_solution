package com.tbs.app.service.business.impl;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.constant.CommonConstant;
import com.tbs.app.mapper.SystemRoleMapper;
import com.tbs.app.pojo.entity.SystemRoleEntity;
import com.tbs.app.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.util.StrUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author abstergo
 */
public class SimpleSystemRoleServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemRoleEntity, Long, SystemRoleMapper>
    implements ISystemRoleService {

    @Resource
    SystemRoleMapper systemRoleMapper;

    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected SystemRoleMapper getMapper() {
        return systemRoleMapper;
    }

    @Override
    protected SystemRoleEntity newEntity() {
        return new SystemRoleEntity();
    }

    @Override
    protected Integer getFlagDisable() {
        return CommonConstant.FLAG_OFF;
    }

    @Override
    protected SystemRoleEntity insertNew(SystemRoleEntity entity) {
        entity.setFlag(CommonConstant.FLAG_ON);
        entity.setCreateTime(new java.util.Date());
        getMapper().insert(entity);
        return entity;
    }

    @CacheEvict(cacheNames = "system_role", key = "#entity.role",
        condition = "#entity!=null&&#entity.role!=null&&#entity.role.length()>0")
    @Override
    protected List<SystemRoleEntity> updateBy(SystemRoleEntity entity) {
        entity.setUpdateTime(new java.util.Date());
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemRoleEntity compareAndUpdateValue(SystemRoleEntity newOne, SystemRoleEntity oldOne) {
        if (newOne.getFlag() != null) {
            if (newOne.getFlag().equals(CommonConstant.FLAG_OFF) || newOne.getFlag().equals(CommonConstant.FLAG_ON)) {
                oldOne.setFlag(newOne.getFlag());
            }
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (StrUtil.isNotEmpty(newOne.getRole())) {
            oldOne.setRole(newOne.getRole());
        }
        return oldOne;
    }

    @Cacheable(cacheNames = "system_role", key = "#roleName")
    @ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
    @Override
    public SystemRoleEntity getRoleByName(@NotNull String roleName) {
        if (StrUtil.isEmpty(roleName)) {
            return null;
        }
        return getQueryWrapper().query().where().equal(SystemRoleEntity::getRole, roleName).pour().fetchOne();
    }
}
