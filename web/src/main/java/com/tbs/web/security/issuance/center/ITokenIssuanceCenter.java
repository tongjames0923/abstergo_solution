package com.tbs.web.security.issuance.center;

import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;

/**
 * 令牌发放中心接口
 * <p>
 * 定义了系统中令牌管理的核心功能，包括令牌的发放、查询和状态管理等操作。 该接口是安全系统中令牌管理模块的核心组件，负责处理用户认证和授权过程中涉及的令牌生命周期管理。
 * </p>
 *
 * @author abstergo
 */
public interface ITokenIssuanceCenter {
    /**
     * 发放令牌
     * <p>
     * 根据用户详细信息、令牌类型和令牌创建类型生成并返回一个新的令牌模型。 此方法通常在用户成功认证后调用，用于生成后续请求中使用的安全令牌。
     * </p>
     *
     * @param form 令牌请求表单，不能为空；其用户信息、用户名、令牌类型、创建方式、会话ID均不能为空
     * @return 生成并已持久化的令牌模型，包含令牌的详细信息
     */
    ITokenModel issueToken(TokenRequestForm form);

    /**
     * 根据令牌字符串获取令牌模型
     * <p>
     * 通过令牌字符串查找对应的令牌模型，用于验证令牌的有效性和获取令牌相关信息。
     * </p>
     *
     * @param token 令牌字符串，不能为空
     * @return 对应的令牌模型，如果令牌不存在或已失效则可能返回null
     */
    ITokenModel getTokenByToken(String token);

    /**
     * 设置令牌状态
     * <p>
     * 更新指定令牌的状态，用于令牌的激活、停用、注销等状态管理。
     * </p>
     *
     * @param token  令牌字符串，不能为空
     * @param status 新的令牌状态，不能为空
     */
    void setTokenStatus(String token, TokenStatusEnum status);

    /**
     * 根据用户名和令牌类型获取用户令牌集合
     * <p>
     * 查询指定用户和令牌类型的所有令牌，用于用户令牌的批量管理和清理操作。
     * </p>
     *
     * @param userName  用户名
     * @param tokenType 令牌类型
     * @return 用户拥有的指定类型的令牌集合
     */
    Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType);

    /**
     * 根据SessionId获取用户令牌集合
     * <p>
     * 获取指定SessionId的所有令牌，用于SessionId对应的令牌管理。
     * </p>
     *
     * @param sessionId SessionId
     * @return SessionId对应的令牌集合
     */
    Collection<ITokenModel> getTokensBySessionId(String sessionId);

    /**
     * 列出指定用户在指定时间段内的SessionId
     * <p>
     * 列出指定用户在指定时间段内的SessionId，用于SessionId对应的令牌管理。
     * </p>
     *
     * @param userName 用户名
     * @param beg      开始时间
     * @param end      结束时间
     * @return SessionId列表
     */
    Collection<ITokenModel> listSessions(String userName, Date beg, Date end);

}
