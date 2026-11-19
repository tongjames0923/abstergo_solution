package com.tbs.app.service.impl.role;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.role.ISystemRoleGroupService;
import com.tbs.app.service.role.ISystemRoleService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.IStartup;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemDataScopeEnum;
import com.tbs.shared.enums.SystemRoleFlagEnum;
import com.tbs.shared.enums.SystemRoleTypeEnum;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemRoleMapper;
import com.tbs.shared.pojo.entity.SystemRoleEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.tbs.app.service.impl.CacheNamesConstants.SYSTEM_ROLE;
import static com.tbs.app.service.impl.CacheNamesConstants.SYSTEM_ROLE_NAME;

/**
 * @author abstergo
 */

@Slf4j
public class SimpleSystemRoleServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemRoleEntity, Long, SystemRoleMapper>
    implements ISystemRoleService, IStartup {

    private static final IAuditService<SystemRoleEntity, Long, Long> auditService = new DefaultAuditService<>();
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
    protected Integer getFlagDeleted() {
        return SystemRoleFlagEnum.DELETE.getCode();
    }

    @Override
    protected SystemRoleEntity insertNew(SystemRoleEntity entity) {

        AssertUtil.isTrue(entity.getType() == null).i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_TYPE_EMPTY)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        AssertUtil.isTrue(!EnumUtil.isValidCode(SystemRoleTypeEnum.class, entity.getType()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_TYPE_INVALID)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getRole())).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_CODE_EMPTY).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getName())).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_NAME_EMPTY).throwIt();

        var old = ((ISystemRoleService)getInstance()).queryByRole(entity.getRole());
        AssertUtil.isTrue(old != null).i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_CODE_EXISTS)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(SystemRoleFlagEnum.ACTIVE.getCode());
        getMapper().insert(entity);
        return entity;
    }

    @Caching(evict = {
        @CacheEvict(cacheNames = SYSTEM_ROLE, key = "#entity.id", condition = "#entity!=null&&#entity.id!=null"),
        @CacheEvict(cacheNames = SYSTEM_ROLE_NAME, allEntries = true)})
    @Override
    public List<SystemRoleEntity> save(SystemRoleEntity entity) {
        return super.save(entity);
    }

    @Override
    protected SystemRoleEntity compareAndUpdateValue(SystemRoleEntity newOne, SystemRoleEntity oldOne) {
        AssertUtil.isTrue(SystemRoleFlagEnum.SYSTEMED.getCode().equals(oldOne.getFlag()))
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_SYSTEM_NOT_MODIFY)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        if (newOne.getFlag() != null) {
            AssertUtil.isTrue(newOne.getFlag().equals(SystemRoleFlagEnum.SYSTEMED.getCode()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_SYSTEM_NOT_SET_BY_API)
                .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
            AssertUtil.isTrue(!CollUtil.isIn(newOne.getFlag(), SystemRoleFlagEnum.ACTIVE.getCode(),
                    SystemRoleFlagEnum.DISABLE.getCode()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_STATUS_INVALID)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            if (newOne.getFlag().equals(SystemRoleFlagEnum.ACTIVE.getCode()) ||
                newOne.getFlag().equals(SystemRoleFlagEnum.DISABLE.getCode())) {
                oldOne.setFlag(newOne.getFlag());
            }
        }
        if (StrUtil.isNotEmpty(newOne.getName())) {
            oldOne.setName(newOne.getName());
        }
        if (StrUtil.isNotEmpty(newOne.getDescription())) {
            oldOne.setDescription(newOne.getDescription());
        }
        if (StrUtil.isNotEmpty(newOne.getRole()) && !Objects.equals(newOne.getRole(), oldOne.getRole())) {
            var old = ((ISystemRoleService)getInstance()).queryByRole(newOne.getRole());
            AssertUtil.isTrue(old != null).i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_CODE_EXISTS)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        if (newOne.getType() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(SystemRoleTypeEnum.class, newOne.getType()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_TYPE_INVALID)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            oldOne.setType(newOne.getType());
        }
        if (newOne.getDataScope() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(SystemDataScopeEnum.class, newOne.getDataScope()))
                .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_DATA_SCOPE_INVALID)
                .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            oldOne.setDataScope(newOne.getDataScope());
        }
        return oldOne;
    }

    @Override
    protected void deletePending(SystemRoleEntity entity) throws FormattedRuntimeException {
        AssertUtil.isTrue(SystemRoleFlagEnum.SYSTEMED.getCode().equals(entity.getFlag()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_SYSTEM_NOT_DELETE).throwIt();
        AssertUtil.isTrue(SpringUtil.getBean(ISystemRoleGroupService.class).isRoleInGroupAnyGroup(entity.getId()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n(com.tbs.app.constant.SystemI18nConstant.PERMISSION_IN_USE_NOT_DELETE).throwIt();
    }

    @Caching(evict = {@CacheEvict(cacheNames = SYSTEM_ROLE, key = "#id"),
        @CacheEvict(cacheNames = SYSTEM_ROLE_NAME, allEntries = true)})

    @Override
    public SystemRoleEntity deleteById(Long id) {
        return super.deleteById(id);
    }

    @Cacheable(cacheNames = SYSTEM_ROLE, key = "#id", unless = "#result==null")
    @ExpireOn(cacheNames = SYSTEM_ROLE, value = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    @Override
    public SystemRoleEntity queryById(Long id) {
        return super.queryById(id);
    }

    @Cacheable(cacheNames = SYSTEM_ROLE_NAME, key = "#role", unless = "#result==null")
    @ExpireOn(cacheNames = SYSTEM_ROLE_NAME, value = 10, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    @Override
    public SystemRoleEntity queryByRole(String role) {
        return getQueryWrapper().query().where().equal(SystemRoleEntity::getRole, role)
            .equal(SystemRoleEntity::getFlag, SystemRoleFlagEnum.ACTIVE.getCode()).pour().fetchOne();
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public void startUp() throws RuntimeException {
        //FIXME 自带默认角色插入。
        //        SystemRoleEntity commonUser = getMapper().selectByPrimaryKey(RoleConstants.NORMAL_USER_ROLE_ID);
        //        if (commonUser == null) {
        //            log.info("系统角色不存在，默认插入...");
        //            getMapper().insert(RoleConstants.NORMAL_USER_ROLE);
        //        } else {
        //            log.info("系统角色已存在...");
        //            getMapper().updateByPrimaryKey(RoleConstants.NORMAL_USER_ROLE);
        //        }
        //        SystemRoleEntity admin = getMapper().selectByPrimaryKey(RoleConstants.SUPER_ADMIN_ROLE_ID);
        //        if (admin == null) {
        //            log.info("系统角色不存在，默认插入...");
        //            getMapper().insert(RoleConstants.SUPER_ADMIN_ROLE);
        //        } else {
        //            log.info("系统角色已存在...");
        //            getMapper().updateByPrimaryKey(RoleConstants.SUPER_ADMIN_ROLE);
        //        }
    }

    @Override
    protected Optional<IAuditService<SystemRoleEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
