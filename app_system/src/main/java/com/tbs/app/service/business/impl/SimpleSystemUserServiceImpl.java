package com.tbs.app.service.business.impl;

import com.tbs.app.annoation.UserNameValid;
import com.tbs.app.constant.CommonConstant;
import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.enums.SystemUserStatusEnum;
import com.tbs.app.mapper.SystemUserMapper;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.app.service.business.IPasswordEncrypt;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.EnumUtil;
import com.tbs.common.util.StrUtil;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简单的系统用户服务实现
 *
 * @author abstergo
 */
public class SimpleSystemUserServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemUser, Long, SystemUserMapper>
    implements ISystemUserService {

    private static SimpleSystemUserServiceImpl instance;

    @Resource
    private IPasswordEncrypt encrypt;

    public SimpleSystemUserServiceImpl() {
        instance = this;
    }

    @Resource
    @Lazy
    private IUserRoleRelationService relationService;

    @Override
    protected SystemUser insertNew(SystemUser entity) {
        AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(entity.getUsername()), "用户名格式错误");
        AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(entity.getPassword()), "密码格式错误");
        AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(entity.getNickname()), "昵称格式错误");
        entity.setPassword(encrypt.encrypt(entity.getPassword()));
        entity.setCreateTime(new Date());
        entity.setFlag(SystemUserStatusEnum.CREATED.getCode());
        getMapper().insert(entity);
        relationService.enableRole(entity.getId(), CommonConstant.NORMAL_USER_ROLE_NAME);
        return entity;
    }

    @Override
    protected List<SystemUser> updateBy(SystemUser entity) {
        entity.setUpdateTime(new Date());
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemUser compareAndUpdateValue(SystemUser systemUser, SystemUser oldSystemUser) {
        if (StrUtil.isNotEmpty(systemUser.getUsername())) {
            AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(systemUser.getUsername()), "用户名格式错误");
            oldSystemUser.setUsername(systemUser.getUsername());
        }
        if (StrUtil.isNotEmpty(systemUser.getPassword())) {
            AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(systemUser.getPassword()), "密码格式错误");
            oldSystemUser.setPassword(encrypt.encrypt(systemUser.getPassword()));
        }
        if (StrUtil.isNotEmpty(systemUser.getNickname())) {
            AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(systemUser.getNickname()), "昵称格式错误");
            oldSystemUser.setNickname(systemUser.getNickname());
        }
        if (systemUser.getFlag() != null) {
            AssertUtil.isTrue(EnumUtil.isValidCode(SystemUserStatusEnum.class, systemUser.getFlag()),
                "用户状态值异常，不存在{}值", systemUser.getFlag());
            oldSystemUser.setFlag(systemUser.getFlag());
        }
        if (StrUtil.isNotEmpty(systemUser.getAvatar())) {
            oldSystemUser.setAvatar(systemUser.getAvatar());
        }
        if (StrUtil.isNotEmpty(systemUser.getRemark())) {
            oldSystemUser.setRemark(systemUser.getRemark());
        }
        oldSystemUser.setUpdateTime(new Date());
        return oldSystemUser;
    }

    @Resource
    IUserNamePasswordValidator userNamePasswordValidator;

    @Override
    public List<SystemUser> queryByUserName(@UserNameValid String username) {
        return getQueryWrapper().query().where().equal(SystemUser::getUsername, username).pour().fetch();
    }

    @Resource
    SystemUserMapper mapper;

    @Override
    protected SystemUserMapper getMapper() {
        return mapper;
    }

    @Override
    protected SystemUser newEntity() {
        return new SystemUser();
    }

    @Override
    protected Integer getFlagDisable() {
        return SystemUserStatusEnum.DISABLED.getCode();
    }
}
