package com.tbs.web.security.token;

import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;

import java.io.Serializable;
import java.util.Date;

/**
 * 令牌模型
 * <p>
 * 表示系统中的安全令牌实体，聚合令牌串、归属用户、类型、状态与生命周期等核心属性。
 * 既是令牌颁发/校验的数据载体，也是缓存与持久化的基本单元。
 * </p>
 *
 * @author abstergo
 * @see TokenTypeEnum 令牌类型
 * @see TokenStatusEnum 令牌状态
 * @see TokenCreateTypeEnum 令牌创建方式
 */
public interface ITokenModel extends Serializable {
    /**
     * 获取令牌串
     *
     * @return 令牌唯一字符串，用于后续请求携带与查询
     */
    String getToken();

    /**
     * 获取令牌过期时间
     *
     * @return 令牌失效的时间点；为 {@code null} 表示未设置过期时间（长期有效）
     */
    Date getExpireTime();

    /**
     * 获取令牌创建时间
     *
     * @return 令牌签发的时间点，用于排序与会话时长统计
     */
    Date getCreateTime();

    /**
     * 获取用户名
     *
     * @return 令牌归属的用户名，用于关联用户身份与授权信息
     */
    String getUserName();

    /**
     * 获取令牌创建方式
     *
     * @return 令牌的创建类型，取值见 {@link TokenCreateTypeEnum}
     */
    TokenCreateTypeEnum getCreateType();

    /**
     * 获取令牌当前状态
     *
     * @return 令牌状态，取值见 {@link TokenStatusEnum}
     */
    TokenStatusEnum getStatus();

    /**
     * 设置令牌状态
     * <p>
     * 用于令牌激活、停用、注销、过期等状态流转；状态变更后需由调用方负责持久化。
     * </p>
     *
     * @param status 新的令牌状态，不能为空，取值见 {@link TokenStatusEnum}
     */
    void setStatus(TokenStatusEnum status);

    /**
     * 获取令牌类型
     *
     * @return 令牌类型，取值见 {@link TokenTypeEnum}（如访问令牌、刷新令牌、应用令牌）
     */
    TokenTypeEnum getTokenType();

    /**
     * 获取会话ID
     *
     * @return 令牌所属会话的标识；为 {@code null} 表示未绑定会话（如独立刷新令牌）
     */
    String getSessionId();

}
