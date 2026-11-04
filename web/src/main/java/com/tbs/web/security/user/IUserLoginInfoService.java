package com.tbs.web.security.user;

import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.vo.LoginSessionVO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 用户登录信息处理服务
 * <p>
 * 负责维护「令牌 ↔ 用户登录信息」之间的映射关系，支撑认证链路中的用户信息获取与登出清理。
 * 存储内容仅包含会话自身的数据（令牌、用户标识、会话标识、当前部门、状态），
 * 用户详情由认证阶段按用户标识实时装配，不在会话维度持久化或缓存。
 * </p>
 *
 * @author abstergo
 * @see ITokenModel 令牌模型
 * @see LoginSessionVO 会话视图对象
 */
public interface IUserLoginInfoService {

    /**
     * 写入登录信息
     * <p>
     * 将令牌模型与用户标识关联落库，供后续 {@link #getUserDetails} 回源读取。
     * 写入失败应抛出异常，避免产生「已登录但查无会话」的脏状态。
     * </p>
     *
     * @param userDetails 用户详情，不能为空
     * @param tokenModel  本次登录签发的令牌模型，不能为空
     */
    void insertUserLoginInfo(UserDetails userDetails, ITokenModel tokenModel);

    /**
     * 用户登出
     * <p>
     * 将指定令牌对应的登录信息置为失效，下次读取将得到空结果。
     * </p>
     *
     * @param token 待登出的令牌串，不能为空
     */
    void removeUserLoginInfo(String token);

    /**
     * 替换用户数据
     * <p>
     * 在令牌不变的情况下更新关联的用户详情（如权限变更后刷新）。
     * </p>
     *
     * @param userDetails 新的用户详情，不能为空
     * @param token       关联的令牌模型，不能为空
     * @deprecated 用户详情不再持久化，本方法不会写入任何用户详情；权限与部门变更在下次认证时自然生效。
     *     计划在下一个大版本移除。
     */
    @Deprecated
    void replaceUserDetails(UserDetails userDetails, ITokenModel token);

    /**
     * 切换当前会话的当前部门
     * <p>
     * 以令牌定位会话，校验目标部门属于该用户后写回存储。
     * 仅影响当前令牌对应的会话，同一用户的其他登录会话不受影响。
     * </p>
     *
     * @param token        令牌模型，不能为空
     * @param departmentId 目标部门标识，不能为空
     */
    void changeCurrentDepartment(ITokenModel token, Long departmentId);

    /**
     * 根据令牌获取用户详情
     * <p>
     * 一个令牌至多关联一个用户，返回列表恒为 0 或 1 个元素，且不会出现 null 元素；
     * 无匹配时返回空集合。
     * </p>
     *
     * @param token 令牌串，不能为空
     * @return 令牌对应的用户详情集合，无匹配时返回空集合
     */
    List<UserDetails> getUserDetails(String token);

    /**
     * 获取用户可用的会话令牌
     * <p>
     * 返回指定用户名下当前有效的会话令牌串，用于会话维度的批量操作。
     * </p>
     *
     * @param username 用户名，不能为空
     * @return 可用会话令牌串列表，无可用会话时返回空集合
     * @deprecated 建议使用 {@link #listUserSessions} 获取更丰富的会话视图
     */
    @Deprecated
    List<String> getUserAvaliableSessionId(String username);

    /**
     * 失效用户会话快照类派生缓存（会话列表与可用会话ID列表）。
     * <p>
     * 令牌签发/状态变更后的连带失效扩展点：会话快照缓存的键布局由实现方自持，
     * 调用方只需给出用户名；无会话快照缓存的实现保持默认空实现。
     * </p>
     *
     * @param username 用户名，空则不处理
     */
    default void evictUserSessionSnapshot(String username) {
    }

    /**
     * 获取用户的登录会话列表
     * <p>
     * 聚合该用户全部会话的令牌，按会话维度组织为 {@link LoginSessionVO} 列表，供前端展示与管理。
     * </p>
     *
     * @param username 用户名，不能为空
     * @return 登录会话视图列表，无会话时返回空集合
     */
    List<LoginSessionVO> listUserSessions(String username);
}
