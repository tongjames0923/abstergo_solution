package com.tbs.app.service.user;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemUserEntity;

import java.util.List;

/**
 * 用户服务
 *
 * @author abstergo
 */
public interface ISystemUserService extends IServiceTemplate<SystemUserEntity, Long> {

    /**
     * 根据用户名查询用户
     *
     * @param username
     * @return
     */
    List<SystemUserEntity> queryByUserName(String username);

    /**
     * 根据用户名查询用户ID
     *
     * @param username
     * @return
     */
    List<Long> queryUserIdsByUserName(String username);

    /**
     * 查询全部激活状态用户的ID（全体广播 fan-out 数据源）
     *
     * @return 激活用户ID列表
     */
    List<Long> queryAllActiveUserIds();

    /**
     * 按用户名前缀搜索激活状态用户的ID（topic 关键字匹配数据源）
     *
     * @param prefix 用户名前缀关键字
     * @return 匹配的用户ID列表
     */
    List<Long> queryUserIdsByUserNamePrefix(String prefix);
}
