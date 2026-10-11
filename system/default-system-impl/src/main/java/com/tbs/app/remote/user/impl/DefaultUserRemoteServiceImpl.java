package com.tbs.app.remote.user.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tbs.app.remote.user.IUserRemoteService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.vo.RemoteUserInfoVO;
import com.tbs.shared.pojo.criteria.CommonSystemUserQueryCriteria;
import com.tbs.shared.pojo.entity.SystemUserEntity;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的用户远程服务实现
 *
 * @author abstergo
 */
public class DefaultUserRemoteServiceImpl implements IUserRemoteService {
    @Resource
    private ISystemUserService userService;

    @Override
    public List<RemoteUserInfoVO> getUserInfoByUserName(String userName) {
        if (StrUtil.isBlank(userName)) {
            return Collections.emptyList();
        }
        return userService.queryUserIdsByUserName(userName).stream().map(id -> {
            var user = userService.queryById(id);
            if (user == null) {
                return null;
            }
            return convertToVO(user);
        }).filter(vo -> vo != null).collect(Collectors.toList());
    }

    @Override
    public RemoteUserInfoVO getUserInfoById(Long userId) {
        if (userId == null) {
            return null;
        }
        SystemUserEntity user = userService.queryById(userId);
        if (user == null) {
            return null;
        }
        return convertToVO(user);
    }

    @Override
    public List<RemoteUserInfoVO> getUserInfoByNickName(String nickName) {
        if (StrUtil.isBlank(nickName)) {
            return Collections.emptyList();
        }
        return userService.queryAndPage(new CommonSystemUserQueryCriteria().setNickNameLike(nickName), null).getData()
            .stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<RemoteUserInfoVO> getUserInfoByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return userIds.stream().map(this::getUserInfoById).filter(vo -> vo != null).collect(Collectors.toList());
    }

    /**
     * 将用户实体转换为VO
     *
     * @param entity 用户实体
     * @return 用户VO
     */
    private RemoteUserInfoVO convertToVO(SystemUserEntity entity) {
        RemoteUserInfoVO vo = new RemoteUserInfoVO();
        vo.setUserName(entity.getUserName());
        vo.setNickName(entity.getNickName());
        vo.setAvatar(entity.getAvatar());
        vo.setRemark(entity.getRemark());
        vo.setFlag(entity.getFlag());
        vo.setUserId(entity.getId());
        return vo;
    }
}
