package com.tbs.app.service.impl.user;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.constant.UserServiceFeatureFlagConstants;
import com.tbs.app.intercept.DataPermissionIntercepter;
import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.IFeatureFlagService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemUserMapper;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.builder.ISqlBuildContext;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 简单的系统用户服务实现
 *
 * @author abstergo
 */
@Slf4j
public class SimpleSystemUserServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemUserEntity, Long, SystemUserMapper>
    implements ISystemUserService {

    @Resource
    IUserNamePasswordValidator userNamePasswordValidator;
    @Resource
    SystemUserMapper mapper;
    @Resource
    private IPasswordEncrypt encrypt;

    @Resource
    private IFeatureFlagService featureFlagService;

    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected SystemUserEntity insertNew(SystemUserEntity entity) {
        if (featureFlagService.isEnableValue(UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR,
            UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR_ENABLE_VALUE)) {
            AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(entity.getUserName()))
                .message(I18nUtil.get("user.name.invalid")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(entity.getPassword()))
                .message(I18nUtil.get("user.password.invalid")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        entity.setPassword(encrypt.encrypt(entity.getPassword()));
        entity.setFlag(SystemUserStatusEnum.LOCKED.getCode());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected List<SystemUserEntity> updateBy(SystemUserEntity entity) {
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Caching(evict = {@CacheEvict(value = "system_user", key = "#entity.userName",
        condition = "#entity!=null&&#entity.userName!=null")})

    @Override
    public List<SystemUserEntity> save(SystemUserEntity entity) {
        return super.save(entity);
    }

    @Override
    protected SystemUserEntity compareAndUpdateValue(SystemUserEntity systemUserEntity,
        SystemUserEntity oldSystemUserEntity) {
        if (StrUtil.isNotEmpty(systemUserEntity.getPassword())) {
            if (featureFlagService.isEnableValue(UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR,
                UserServiceFeatureFlagConstants.USER_NAME_PASSWORD_VALIDATOR_ENABLE_VALUE)) {
                AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(systemUserEntity.getPassword()))
                    .message(I18nUtil.get("user.password.invalid")).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            }

            if (!systemUserEntity.getPassword().equals(oldSystemUserEntity.getPassword())) {
                oldSystemUserEntity.setPassword(encrypt.encrypt(systemUserEntity.getPassword()));
                AuthUtil.getInstance().notifyUserAuthStatusChanged(oldSystemUserEntity.getUserName());
            }
        }
        if (StrUtil.isNotEmpty(systemUserEntity.getUserName()) &&
            !Objects.equals(oldSystemUserEntity.getUserName(), systemUserEntity.getUserName())) {
            throw FormattedRuntimeException.userInputError(I18nUtil.get("user.name.not.changeable"));
        }

        if (systemUserEntity.getFlag() != null) {
            if (!Objects.equals(systemUserEntity.getFlag(), oldSystemUserEntity.getFlag())) {
                AssertUtil.isTrue(!EnumUtil.isValidCode(SystemUserStatusEnum.class, systemUserEntity.getFlag()))
                    .message(I18nUtil.get("user.status.invalid", systemUserEntity.getFlag()))
                    .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
                oldSystemUserEntity.setFlag(systemUserEntity.getFlag());
                AuthUtil.getInstance().notifyUserAuthStatusChanged(oldSystemUserEntity.getUserName());
            }
        }
        if (StrUtil.isNotEmpty(systemUserEntity.getAvatar())) {
            oldSystemUserEntity.setAvatar(systemUserEntity.getAvatar());
        }
        if (StrUtil.isNotEmpty(systemUserEntity.getRemark())) {
            oldSystemUserEntity.setRemark(systemUserEntity.getRemark());
        }
        if (StrUtil.isNotEmpty(systemUserEntity.getNickName())) {
            oldSystemUserEntity.setNickName(systemUserEntity.getNickName());
        }
        return oldSystemUserEntity;
    }

    @Override
    protected void inUseDataScope(ISqlBuildContext context) {
        DataPermissionIntercepter.setEnableFlag(context, true);
    }

    @Override
    public boolean useDataScope() {
        return true;
    }

    @Cacheable(value = "system_user", key = "#id")
    @ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
    @Override
    public SystemUserEntity queryById(Long id) {
        return super.queryById(id);
    }

    @Override
    protected SystemUserMapper getMapper() {
        return mapper;
    }

    @Override
    protected SystemUserEntity newEntity() {
        return new SystemUserEntity();
    }

    @Override
    protected Integer getFlagDeleted() {
        return SystemUserStatusEnum.INACTIVE.getCode();
    }

    @Override
    public List<SystemUserEntity> queryByUserName(String username) {
        return ((ISystemUserService)getInstance()).queryUserIdsByUserName(username).stream().map(this::queryById)
            .collect(Collectors.toList());
    }

    @Cacheable(cacheNames = "user_name_id_map", key = "#username")
    @ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<Long> queryUserIdsByUserName(String username) {
        var users = getQueryWrapper().query().where().equal(SystemUserEntity::getUserName, username).pour().fetch();
        if (CollUtil.isEmpty(users)) {
            return new ArrayList<>();
        }
        return users.stream().map(SystemUserEntity::getId).collect(Collectors.toList());
    }

    private static final IAuditService<SystemUserEntity, Long, Long> auditService = new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<SystemUserEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
