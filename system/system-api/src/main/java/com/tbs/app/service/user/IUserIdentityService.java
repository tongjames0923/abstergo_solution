package com.tbs.app.service.user;

import com.tbs.app.service.user.model.UserIdentity;

/**
 * 用户身份聚合服务
 * <p>
 * 安全层获取用户身份的唯一入口：一次调用返回认证所需的全部数据（状态位、权限编码、部门归属）。
 * 它存在的目的是隔离模块边界——若让安全层分别调用用户、角色、部门三个源服务，单体部署下是三次索引查询，
 * 一旦某个源服务被拆为独立进程就会变成三次串行远程调用，模块拓扑将直接泄漏进认证链路。
 * </p>
 * <p>
 * 实现约束：
 * <ul>
 *     <li>内部只允许调用同进程的本地服务完成装配，禁止在实现中引用其他模块的远程服务；</li>
 *     <li>不缓存装配结果，缓存只能存在于拥有数据的源服务内部，由该服务自行失效；</li>
 *     <li>每次调用都返回新实例，不得返回可能被多个调用方共享的对象。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @see UserIdentity 身份视图
 */
public interface IUserIdentityService {

    /**
     * 按用户标识装载身份视图
     *
     * @param userId 用户标识，不能为空
     * @return 身份视图；用户不存在或已删除时返回 {@code null}
     */
    UserIdentity loadUserIdentity(Long userId);
}
