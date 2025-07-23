package com.tbs.app.service.business.impl;

import com.tbs.app.constant.CommonConstant;
import com.tbs.app.mapper.UserRoleRelationMapper;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.pojo.entity.UserRoleRelation;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.sql.ef.IEntityQueryWrapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tongj
 */
public class SimpleUserRoleRelationServiceImpl implements IUserRoleRelationService {

    @Lazy
    @Resource
    private ISystemUserService systemUserService;

    @Resource
    @Lazy
    private ISystemRoleService systemRoleService;

    @Resource
    private IEntityQueryWrapper<UserRoleRelation> wrapper;

    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    @PostConstruct
    void init() {
        wrapper.makeQueryable(new UserRoleRelation());
    }

    @Override
    @CacheEvict(cacheNames = "user_role", key = "#userId")
    public void enableRole(@NotNull Long userId, @NotNull String role) {
        setRole(userId, role, CommonConstant.FLAG_ON);
    }

    private void setRole(@NotNull Long userId, @NotNull String role, @NotNull Integer flag) {
        AssertUtil.hasText(role, "角色不能为空");
        SystemUser user = systemUserService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        SystemRole roleEntity = systemRoleService.getRoleByName(role);
        AssertUtil.notNull(roleEntity, "角色不存在");
        AssertUtil.isTrue(!CommonConstant.FLAG_ON.equals(roleEntity.getFlag()), "角色 {} 已失效", role);
        UserRoleRelation relation = wrapper.query().where().equal(UserRoleRelation::getUserId, userId)
            .equal(UserRoleRelation::getRoleId, roleEntity.getId()).pour().fetchOne();
        if (relation == null) {
            relation = new UserRoleRelation();
            relation.setUserId(userId);
            relation.setRoleId(roleEntity.getId());
            relation.setFlag(flag);
            relation.setCreateTime(new Date());
            userRoleRelationMapper.insert(relation);
        } else {
            relation.setFlag(flag);
            relation.setUpdateTime(new Date());
            userRoleRelationMapper.updateByPrimaryKey(relation);
        }
    }

    @Override
    @CacheEvict(cacheNames = "user_role", key = "#userId")
    public void disableRole(@NotNull Long userId, @NotNull String roleId) {
        setRole(userId, roleId, CommonConstant.FLAG_OFF);
    }

    @Cacheable(cacheNames = "user_role", key = "#userId")
    @Override
    public List<SystemRole> queryUserRoles(@NotNull Long userId) {
        List<UserRoleRelation> relations = wrapper.query().where().equal(UserRoleRelation::getUserId, userId)
            .equal(UserRoleRelation::getFlag, CommonConstant.FLAG_ON).pour().fetch();
        if (CollUtil.isEmpty(relations)) {
            return Arrays.asList();
        }
        return relations.stream().map(relation -> systemRoleService.queryById(relation.getRoleId()))
            .filter(role -> role != null && CommonConstant.FLAG_ON.equals(role.getFlag())).distinct()
            .collect(Collectors.toList());
    }
}
