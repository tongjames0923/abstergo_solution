package com.tbs.app.service.genericapi;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.web.genric.IApiInstancePreAuth;

import java.util.List;

/**
 * 泛化API配置服务接口
 * <p>
 * 提供泛化API配置的增删改查及启停用功能
 * </p>
 *
 * @author abstergo
 */
public interface IGenericApiService extends IServiceTemplate<SystemGenericApiEntity, Long> {

    /**
     * 获取所有可用的预认证列表
     *
     * @return
     */
    List<IApiInstancePreAuth> getAllAvaliablePreAuthList();

    /**
     * 根据API ID查询配置
     *
     * @param apiId API唯一标识
     * @return 泛化API配置实体
     */
    SystemGenericApiEntity getByApiId(String apiId);

    /**
     * 取可被泛化调用的 API 配置
     * <p>
     * 「配置存在」与「已启用」两条规则集中在服务层断言，新版泛化调用入口只做取数与映射。
     * </p>
     *
     * @param apiId API唯一标识
     * @return 已启用的泛化API配置，不会为 {@code null}
     */
    SystemGenericApiEntity getInvokableApi(String apiId);

    /**
     * 取可从匿名入口泛化调用的 API 配置
     * <p>
     * 在 {@link #getInvokableApi(String)} 基础上追加免认证约束：登记为需要认证的 API 禁止走匿名入口。
     * </p>
     *
     * @param apiId API唯一标识
     * @return 已启用且无需认证的泛化API配置，不会为 {@code null}
     */
    SystemGenericApiEntity getPublicInvokableApi(String apiId);

    /**
     * 启用API配置
     *
     * @param apiId API唯一标识
     */
    void enableApi(String apiId);

    /**
     * 禁用API配置
     *
     * @param apiId API唯一标识
     */
    void disableApi(String apiId);
}
