package com.tbs.app.service.identity;

import com.tbs.common.data.PageVO;
import com.tbs.shared.pojo.entity.SystemUserExternalIdentityEntity;

import java.util.List;

/**
 * 外部身份绑定管理服务契约
 * <p>
 * 绑定规则的单点收敛：按身份查询、自动关联（查绑定 → 未命中走策略链 → 写绑定）、
 * 手工绑定/解绑、按用户查询与分页管理。绑定语义约定：
 * 一个外部身份最多绑定一个系统用户；一个系统用户在一个平台最多持有一个外部身份（重复绑定默认替换）。
 * </p>
 *
 * @author abstergo
 * @see IExternalIdentityResolver 自动关联策略 SPI
 */
public interface IUserExternalIdentityService {

    /**
     * 按外部身份查询绑定的系统用户
     *
     * @param identityType 外部身份类型
     * @param externalId   外部身份标识
     * @return 绑定的系统用户 ID；未绑定返回 {@code null}
     */
    Long getUserIdByIdentity(String identityType, String externalId);

    /**
     * 自动关联并绑定
     * <p>
     * 先查既有绑定；未命中时依次执行自动关联策略链，命中即落库绑定并返回用户 ID；
     * 全部未命中返回 {@code null}（调用方据此走「未绑定」提示或自助绑定引导）。
     * </p>
     *
     * @param profile 第三方用户信息载体
     * @return 关联成功的系统用户 ID；无匹配返回 {@code null}
     */
    Long autoBind(ExternalUserProfile profile);

    /**
     * 手工绑定
     * <p>
     * 幂等：同身份同用户重复绑定直接成功；外部身份已绑定其他用户时报错；
     * 同用户同平台已绑定其他外部身份时替换旧绑定。
     * </p>
     *
     * @param identityType 外部身份类型
     * @param externalId   外部身份标识
     * @param userId       系统用户 ID
     */
    void bind(String identityType, String externalId, Long userId);

    /**
     * 解除指定用户在指定平台的绑定
     *
     * @param identityType 外部身份类型
     * @param userId       系统用户 ID
     */
    void unbind(String identityType, Long userId);

    /**
     * 查询指定用户的全部外部身份绑定
     *
     * @param userId 系统用户 ID
     * @return 绑定列表，无绑定时返回空列表
     */
    List<SystemUserExternalIdentityEntity> queryByUserId(Long userId);

    /**
     * 分页查询绑定关系（管理端）
     *
     * @param identityType 外部身份类型过滤，为空时不过滤
     * @param userName     系统登录用户名过滤（内部解析为用户），为空时不过滤
     * @param pageNum      页码，从 1 起
     * @param pageSize     每页数量
     * @return 分页结果（总数与当前页绑定列表）
     */
    PageVO<SystemUserExternalIdentityEntity> queryPage(String identityType, String userName, int pageNum, int pageSize);
}
