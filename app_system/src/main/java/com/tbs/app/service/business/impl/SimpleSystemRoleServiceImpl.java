package com.tbs.app.service.business.impl;

import com.tbs.app.constant.CommonConstant;
import com.tbs.app.mapper.SystemRoleMapper;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.common.util.StrUtil;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author abstergo
 */
public class SimpleSystemRoleServiceImpl
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemRole, Long, SystemRoleMapper>
    implements ISystemRoleService {

    @Resource
    SystemRoleMapper systemRoleMapper;

    @Override
    protected SystemRoleMapper getMapper() {
        return systemRoleMapper;
    }

    @Override
    protected SystemRole newEntity() {
        return new SystemRole();
    }

    @Override
    protected Integer getFlagDisable() {
        return CommonConstant.FLAG_OFF;
    }

    @Override
    protected SystemRole insertNew(SystemRole entity) {
        entity.setFlag(CommonConstant.FLAG_ON);
        entity.setCreateTime(new java.util.Date());
        getMapper().insert(entity);
        return entity;
    }

    @Override
    protected List<SystemRole> updateBy(SystemRole entity) {
        entity.setUpdateTime(new java.util.Date());
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemRole compareAndUpdateValue(SystemRole newOne, SystemRole oldOne) {
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
}
