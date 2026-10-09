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
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
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

import static com.tbs.app.service.impl.CacheNamesConstants.SYSTEM_USER;
import static com.tbs.app.service.impl.CacheNamesConstants.USER_NAME_ID_MAP;
import static com.tbs.app.service.impl.CacheNamesConstants.USER_NAME_TO_ID;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.tbs.app.constant.SystemI18nConstant;

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
                .message(I18nUtil.get(com.tbs.app.constant.SystemI18nConstant.USER_NAME_INVALID)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(entity.getPassword()))
                .message(I18nUtil.get(com.tbs.app.constant.SystemI18nConstant.USER_PASSWORD_INVALID)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        }
        AssertUtil.isTrue(
                !CollUtil.isEmpty(SpringUtil.getBean(ISystemUserService.class).queryByUserName(entity.getUserName())))
            .message(I18nUtil.get(com.tbs.app.constant.SystemI18nConstant.USER_NAME_ALREADY_EXISTS, entity.getUserName()))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
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

    @Caching(
        evict = {@CacheEvict(value = SYSTEM_USER, key = "#entity.id", condition = "#entity!=null&&#entity.id!=null"),
            @CacheEvict(value = USER_NAME_ID_MAP, key = "#entity.userName",
                condition = "#entity!=null&&#entity.userName!=null"),
            @CacheEvict(value = USER_NAME_TO_ID, key = "#entity.userName",
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
                    .message(I18nUtil.get(com.tbs.app.constant.SystemI18nConstant.USER_PASSWORD_INVALID)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
            }

            if (!systemUserEntity.getPassword().equals(oldSystemUserEntity.getPassword())) {
                oldSystemUserEntity.setPassword(encrypt.encrypt(systemUserEntity.getPassword()));
                AuthUtil.getInstance().notifyUserAuthStatusChanged(oldSystemUserEntity.getUserName());
            }
        }
        if (systemUserEntity.getFlag() != null) {
            if (!Objects.equals(systemUserEntity.getFlag(), oldSystemUserEntity.getFlag())) {
                AssertUtil.isTrue(!EnumUtil.isValidCode(SystemUserStatusEnum.class, systemUserEntity.getFlag()))
                    .message(I18nUtil.get(com.tbs.app.constant.SystemI18nConstant.USER_STATUS_INVALID, systemUserEntity.getFlag()))
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

    @Cacheable(value = SYSTEM_USER, key = "#id")
    @ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
    @Override
    public SystemUserEntity queryById(Long id) {
        return super.queryById(id);
    }

    /**
     * 根据用户名查询单个用户ID（用于唯一性校验）
     *
     * @param username 用户名
     * @return 用户ID，不存在返回null
     */
    @Cacheable(cacheNames = USER_NAME_TO_ID, key = "#username", unless = "#result==null")
    @ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
    public Long getUserIdByUserName(String username) {
        var users = getQueryWrapper().query().where().equal(SystemUserEntity::getUserName, username)
            .equal(SystemUserEntity::getFlag, SystemUserStatusEnum.ACTIVE.getCode()).pour().fetch();
        if (CollUtil.isEmpty(users)) {
            return null;
        }
        return users.get(0).getId();
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
        return SystemUserStatusEnum.DELETE.getCode();
    }

    /**
     * 删除用户（逻辑删除），同时清除相关缓存
     *
     * @param id 用户ID
     * @return 被删除的用户实体
     */
    @Caching(evict = {@CacheEvict(value = SYSTEM_USER, key = "#id"),
        @CacheEvict(value = USER_NAME_ID_MAP, allEntries = true),
        @CacheEvict(value = USER_NAME_TO_ID, allEntries = true)})
    @Override
    public SystemUserEntity deleteById(Long id) {
        return super.deleteById(id);
    }

    /**
     * 批量删除用户（逻辑删除），同时清除相关缓存
     *
     * @param ids 用户ID集合
     * @return 删除的用户数量
     */
    @Caching(evict = {@CacheEvict(value = SYSTEM_USER, allEntries = true),
        @CacheEvict(value = USER_NAME_ID_MAP, allEntries = true),
        @CacheEvict(value = USER_NAME_TO_ID, allEntries = true)})
    @Override
    public int deleteByIds(java.util.Collection<Long> ids) {
        return super.deleteByIds(ids);
    }

    @Override
    public List<SystemUserEntity> queryByUserName(String username) {
        var userIds = ((ISystemUserService)getInstance()).queryUserIdsByUserName(username);
        if (CollUtil.isEmpty(userIds)) {
            return new ArrayList<>();
        }
        return userIds.stream().map(getInstance()::queryById).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Cacheable(cacheNames = USER_NAME_ID_MAP, key = "#username")
    @ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<Long> queryUserIdsByUserName(String username) {
        var users = getQueryWrapper().query().where().equal(SystemUserEntity::getUserName, username).pour().fetch();
        if (CollUtil.isEmpty(users)) {
            return new ArrayList<>();
        }
        return users.stream().map(SystemUserEntity::getId).collect(Collectors.toList());
    }

    /**
     * 查询全部激活状态用户ID（短缓存，兼顾广播 fan-out 性能与新增用户可见性）
     *
     * @return 激活用户ID列表
     */
    @Cacheable(cacheNames = SYSTEM_USER, key = "'allActiveIds'")
    @ExpireOn(value = 1, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<Long> queryAllActiveUserIds() {
        var users = getQueryWrapper().query().where()
            .equal(SystemUserEntity::getFlag, SystemUserStatusEnum.ACTIVE.getCode()).pour().fetch();
        return users.stream().map(SystemUserEntity::getId).collect(Collectors.toList());
    }

    /**
     * 按用户名前缀搜索激活状态用户ID
     *
     * @param prefix 用户名前缀关键字
     * @return 匹配的用户ID列表
     */
    @Cacheable(cacheNames = USER_NAME_ID_MAP, key = "'namePrefix:' + #prefix")
    @ExpireOn(value = 15, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<Long> queryUserIdsByUserNamePrefix(String prefix) {
        if (StrUtil.isEmpty(prefix)) {
            return new ArrayList<>();
        }
        var users = getQueryWrapper().query().where()
            .leftLike(SystemUserEntity::getUserName, prefix)
            .equal(SystemUserEntity::getFlag, SystemUserStatusEnum.ACTIVE.getCode()).pour().fetch();
        return users.stream().map(SystemUserEntity::getId).collect(Collectors.toList());
    }

    private static final IAuditService<SystemUserEntity, Long, Long> auditService = new DefaultAuditService<>();

    @Override
    protected Optional<IAuditService<SystemUserEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}
