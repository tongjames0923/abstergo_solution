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
