package com.tbs.web.security.user;

import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.vo.LoginSessionVO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 用户登录信息处理服务
 * <p>
 * 负责维护「令牌 ↔ 用户登录详情」之间的映射关系，支撑认证链路中的用户详情获取与登出清理。
 * 典型实现会结合缓存（详见各实现类上的 {@code @Cacheable}/{@code @CacheEvict}）降低回源开销。
 * </p>
 *
 * @author abstergo
 * @see ITokenModel 令牌模型
 * @see LoginSessionVO 会话视图对象
 */
public interface IUserLoginInfoService {

    /**
     * 用户登录成功后写入登录信息
     * <p>
     * 将用户详情与令牌模型关联落库（及缓存），供后续 {@link #getUserDetails} 回源读取。
     * 写入失败应抛出异常，避免产生「已登录但查无详情」的脏状态。
     * </p>
     *
     * @param userDetails 用户详情，不能为空
     * @param tokenModel  本次登录签发的令牌模型，不能为空
     */
    void insertUserLoginInfo(UserDetails userDetails, ITokenModel tokenModel);

    /**
     * 用户登出
     * <p>
     * 清除指定令牌对应的登录信息（置为失效并失效相关缓存），下次读取将回源得到空结果。
     * </p>
     *
     * @param token 待登出的令牌串，不能为空
     */
    void removeUserLoginInfo(String token);

    /**
     * 替换用户数据
     * <p>
     * 在令牌不变的情况下更新关联的用户详情（如权限变更后刷新），并失效旧缓存。
     * </p>
     *
     * @param userDetails 新的用户详情，不能为空
     * @param token       关联的令牌模型，不能为空
     */
    void replaceUserDetails(UserDetails userDetails, ITokenModel token);

    /**
     * 切换当前会话的当前部门
     * <p>
     * 以令牌定位会话，校验目标部门属于该用户后写回存储并失效缓存。
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
     * 无匹配时返回空集合。空集合同样会进入缓存，用于抵御无效令牌的重复回源。
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
