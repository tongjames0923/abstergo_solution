package com.tbs.app.remote.user;

import com.tbs.app.vo.RemoteUserInfoVO;

import java.util.List;

/**
 * 远程用户服务接口
 *
 * @author abstergo
 */
public interface IUserRemoteService {

    /**
     * 根据用户名获取用户信息列表
     *
     * @param userName 用户名
     * @return 用户信息列表
     */
    List<RemoteUserInfoVO> getUserInfoByUserName(String userName);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    RemoteUserInfoVO getUserInfoById(Long userId);

    /**
     * 根据昵称模糊查询用户信息列表
     *
     * @param nickName 昵称（支持模糊查询）
     * @return 用户信息列表
     */
    List<RemoteUserInfoVO> getUserInfoByNickName(String nickName);

    /**
     * 批量根据用户ID获取用户信息列表
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表
     */
    List<RemoteUserInfoVO> getUserInfoByIds(List<Long> userIds);
}
