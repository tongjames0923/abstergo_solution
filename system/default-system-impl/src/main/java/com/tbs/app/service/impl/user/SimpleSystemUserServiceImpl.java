package com.tbs.app.service.impl.user;

import com.tbs.app.config.AppSettingProperty;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.interfaces.constraint.IUserNamePasswordValidator;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.ITimeAuditService;
import com.tbs.shared.interfaces.service.SimpleTimeAuditServiceImpl;
import com.tbs.shared.mapper.SystemUserMapper;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.security.user.IPasswordEncrypt;
import com.tbs.web.uitl.AuthUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 简单的系统用户服务实现
 *
 * @author abstergo
 */
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
    @Lazy
    private IUserRoleRelationService relationService;
    @Resource
    private AppSettingProperty property;

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return property.getMainDelegateInvokerImpl();
    }

    @Override
    protected SystemUserEntity insertNew(SystemUserEntity entity) {
        AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(entity.getUserName()), "用户名格式错误");
        AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(entity.getPassword()), "密码格式错误");
        AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(entity.getNickName()), "昵称格式错误");
        entity.setPassword(encrypt.encrypt(entity.getPassword()));
        entity.setFlag(SystemUserStatusEnum.LOCKED.getCode());
        getMapper().insert(entity);
        relationService.enableRole(entity.getId(), RoleConstants.NORMAL_USER_ROLE_ID);
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
            AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(systemUserEntity.getPassword()),
                "密码格式错误");
            if (!systemUserEntity.getPassword().equals(oldSystemUserEntity.getPassword())) {
                oldSystemUserEntity.setPassword(encrypt.encrypt(systemUserEntity.getPassword()));
                AuthUtil.getInstance().notifyUserAuthStatusChanged(oldSystemUserEntity.getUserName());
            }
        }
        if (StrUtil.isNotEmpty(systemUserEntity.getNickName())) {
            AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(systemUserEntity.getNickName()),
                "昵称格式错误");
            oldSystemUserEntity.setNickName(systemUserEntity.getNickName());
        }
        if (systemUserEntity.getFlag() != null) {
            if (systemUserEntity.getFlag() != oldSystemUserEntity.getFlag()) {
                AssertUtil.isTrue(!EnumUtil.isValidCode(SystemUserStatusEnum.class, systemUserEntity.getFlag()),
                    "用户状态值异常，不存在{}值", systemUserEntity.getFlag());
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
        return oldSystemUserEntity;
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
    protected Integer getFlagDisable() {
        return SystemUserStatusEnum.DISABLED.getCode();
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

    @Override
    protected Optional<ITimeAuditService<SystemUserEntity, Long>> getTimeAuditServiceSupport() {
        return Optional.of(new SimpleTimeAuditServiceImpl<SystemUserEntity>());
    }
}
