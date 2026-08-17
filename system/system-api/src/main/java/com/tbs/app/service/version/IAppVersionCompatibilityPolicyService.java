package com.tbs.app.service.version;

import com.tbs.app.vo.AppVersionUpdateCheckVO;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.version.AppVersionCompatibilityPolicyEntity;

/**
 * 应用版本兼容策略服务
 * <p>
 * 提供跨应用版本兼容策略的维护与评估能力：
 * 源应用发布新版本后可通过策略对目标应用提出最低版本要求，
 * 目标应用客户端通过升级检查获得更新要求（强制更新/推荐更新/仅提示）
 *
 * @author abstergo
 */
public interface IAppVersionCompatibilityPolicyService
        extends IServiceTemplate<AppVersionCompatibilityPolicyEntity, Long> {

    /**
     * 启用兼容策略
     *
     * @param id 策略ID
     */
    void enablePolicy(Long id);

    /**
     * 禁用兼容策略
     *
     * @param id 策略ID
     */
    void disablePolicy(Long id);

    /**
     * 评估目标应用的升级要求
     * <p>
     * 综合最新已发布版本与生效的兼容策略，判定目标应用客户端是否需要升级以及升级动作强度：
     * 先比对该应用最新已发布版本，再匹配以该应用为目标的启用状态策略，
     * 取命中的最高强制程度作为最终动作
     *
     * @param appKey             目标应用标识
     * @param platform           目标平台标识，如 ANDROID、IOS、WEB
     * @param currentVersionCode 目标应用当前内部版本号
     * @return 升级检查结果，包含是否需要升级、动作类型与最新版本信息
     */
    AppVersionUpdateCheckVO evaluateCompatibility(String appKey, String platform, Long currentVersionCode);
}
