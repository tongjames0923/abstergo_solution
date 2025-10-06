package com.tbs.app.service.impl.role;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.SystemRoleMapper;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author abstergo
 */

@Slf4j
public class SimpleSystemRoleServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemRoleEntity, Long, SystemRoleMapper>
    implements ISystemRoleService, IStartup {

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
        entity.setFlag(SystemRoleFlagEnum.ACTIVE.getCode());
        entity.setCreateTime(new java.util.Date());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected List<SystemRoleEntity> updateBy(SystemRoleEntity entity) {
        entity.setUpdateTime(new java.util.Date());
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @CacheEvict(cacheNames = "system_role", key = "#entity.id", condition = "#entity!=null&&#entity.id!=null")
    @Override
    public List<SystemRoleEntity> save(SystemRoleEntity entity) {
        return super.save(entity);
    }

    @Override
    protected SystemRoleEntity compareAndUpdateValue(SystemRoleEntity newOne, SystemRoleEntity oldOne) {
        AssertUtil.isTrue(SystemRoleFlagEnum.SYSTEMED.getCode().equals(oldOne.getFlag()), "系统角色不允许修改");
        if (newOne.getFlag() != null) {
            AssertUtil.isTrue(newOne.getFlag().equals(SystemRoleFlagEnum.SYSTEMED.getCode()),
                "不允许接口设置角色属性到系统角色");
            if (newOne.getFlag().equals(SystemRoleFlagEnum.ACTIVE.getCode()) ||
                newOne.getFlag().equals(SystemRoleFlagEnum.DISABLE.getCode())) {
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

    @CacheEvict(cacheNames = "system_role", key = "#id")
    @Override
    public SystemRoleEntity deleteById(Long id) {
        return super.deleteById(id);
    }

    @Cacheable(cacheNames = "system_role", key = "#id", unless = "#result==null")
    @ExpireOn(cacheNames = "system_role", value = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    @Override
    public SystemRoleEntity queryById(Long id) {
        return super.queryById(id);
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public void startUp() throws RuntimeException {
        SystemRoleEntity commonUser = getMapper().selectByPrimaryKey(RoleConstants.NORMAL_USER_ROLE_ID);
        if (commonUser == null) {
            log.info("系统角色不存在，默认插入...");
            getMapper().insert(RoleConstants.NORMAL_USER_ROLE);
        } else {
            log.info("系统角色已存在...");
            getMapper().updateByPrimaryKey(RoleConstants.NORMAL_USER_ROLE);
        }
        SystemRoleEntity admin = getMapper().selectByPrimaryKey(RoleConstants.SUPER_ADMIN_ROLE_ID);
        if (admin == null) {
            log.info("系统角色不存在，默认插入...");
            getMapper().insert(RoleConstants.SUPER_ADMIN_ROLE);
        } else {
            log.info("系统角色已存在...");
            getMapper().updateByPrimaryKey(RoleConstants.SUPER_ADMIN_ROLE);
        }
    }
}
