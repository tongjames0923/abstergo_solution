package com.tbs.app.service.impl;

import com.tbs.app.annoation.UserNameValid;
import com.tbs.app.constraint.IUserNamePasswordValidator;
import com.tbs.app.enums.SystemUserStatusEnum;
import com.tbs.app.mapper.SystemUserMapper;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.AbstractService;
import com.tbs.app.service.ISystemUserService;
import com.tbs.common.util.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简单的系统用户服务实现
 *
 * @author abstergo
 */
public class SimpleSystemUserServiceImpl extends AbstractService<SystemUser, SystemUserMapper>
    implements ISystemUserService {

    private static SimpleSystemUserServiceImpl instance;

    public SimpleSystemUserServiceImpl() {
        instance = this;
    }

    @Resource
    IUserNamePasswordValidator userNamePasswordValidator;

    @Override
    public List<SystemUser> queryByUserName(@UserNameValid String username) {
        return getQueryWrapper().query().where().equal(SystemUser::getUsername, username).pour().fetch();
    }

    @Override
    public List<SystemUser> save(SystemUser systemUser) {
        AssertUtil.notNull(systemUser, "systemUser can not be null");
        if (systemUser.getId() == null) {
            AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(systemUser.getUsername()), "用户名格式错误");
            AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(systemUser.getPassword()), "密码格式错误");
            AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(systemUser.getNickname()), "昵称格式错误");
            systemUser.setCreateTime(new Date());
            getMapper().insert(systemUser);
            return List.of(systemUser);
        } else {
            SystemUser oldSystemUser = getMapper().selectByPrimaryKey(systemUser.getId());
            AssertUtil.notNull(oldSystemUser, "用户不存在");
            if (StrUtil.isNotEmpty(systemUser.getUsername())) {
                AssertUtil.isTrue(!userNamePasswordValidator.validateUserName(systemUser.getUsername()),
                    "用户名格式错误");
                oldSystemUser.setUsername(systemUser.getUsername());
            }
            if (StrUtil.isNotEmpty(systemUser.getPassword())) {
                AssertUtil.isTrue(!userNamePasswordValidator.validatePassword(systemUser.getPassword()),
                    "密码格式错误");
                oldSystemUser.setPassword(systemUser.getPassword());
            }
            if (StrUtil.isNotEmpty(systemUser.getNickname())) {
                AssertUtil.isTrue(!userNamePasswordValidator.validateNickName(systemUser.getNickname()),
                    "昵称格式错误");
                oldSystemUser.setNickname(systemUser.getNickname());
            }
            if (systemUser.getFlag() != null) {
                oldSystemUser.setFlag(systemUser.getFlag());
            }
            if (StrUtil.isNotEmpty(systemUser.getAvatar())) {
                oldSystemUser.setAvatar(systemUser.getAvatar());
            }
            if (StrUtil.isNotEmpty(systemUser.getRemark())) {
                oldSystemUser.setRemark(systemUser.getRemark());
            }
            oldSystemUser.setUpdateTime(new Date());
            getMapper().updateByPrimaryKey(oldSystemUser);
            return List.of(oldSystemUser);
        }

    }

    @Override
    protected SystemUser newEntity() {
        return new SystemUser();
    }

    @Override
    public SystemUser deleteById(Long id) {
        SystemUser user = new SystemUser();
        user.setId(id);
        user.setFlag(SystemUserStatusEnum.DISABLED.getCode());
        return save(user).get(0);
    }

    @Override
    public int deleteByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        List<SystemUser> users = getMapper().selectByIdList(ids.stream().collect(Collectors.toList()));
        if (CollUtil.isEmpty(users)) {
            return 0;
        }
        for (SystemUser user : users) {
            user.setFlag(SystemUserStatusEnum.DISABLED.getCode());
        }
        getMapper().batchUpdate(users);
        return users.size();
    }
}
