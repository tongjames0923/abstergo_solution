package com.tbs.app.service;

import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.app.service.genericapi.ISystemGenericApiParamService;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;
import com.tbs.web.genric.config.GenericApiConfig;
import com.tbs.web.genric.config.GenericApiParamConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 泛化调用配置加载器（接入侧装配）
 * <p>
 * 把主表配置与入参定义子表拼成 {@code web} 侧需要的中立模型。{@code web} 不依赖实体包，因此映射放在这里；
 * 存在性、启用态、免认证约束等规则已由 Service 层断言完成，本类只做取数与转换。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class GenericApiConfigLoader {

    @Resource
    private IGenericApiService genericApiService;

    @Resource
    private ISystemGenericApiParamService genericApiParamService;

    /**
     * 加载可被泛化调用的 API 配置（需认证入口）
     *
     * @param apiId API唯一标识
     * @return 泛化调用配置，含登记的入参定义
     */
    public GenericApiConfig loadInvokable(String apiId) {
        return toConfig(genericApiService.getInvokableApi(apiId));
    }

    /**
     * 加载可从匿名入口泛化调用的 API 配置
     *
     * @param apiId API唯一标识
     * @return 泛化调用配置，含登记的入参定义
     */
    public GenericApiConfig loadPublicInvokable(String apiId) {
        return toConfig(genericApiService.getPublicInvokableApi(apiId));
    }

    /**
     * 实体到中立模型的转换
     *
     * @param apiConfig 主表配置，不能为空
     * @return 泛化调用配置
     */
    private GenericApiConfig toConfig(SystemGenericApiEntity apiConfig) {
        GenericApiConfig config = new GenericApiConfig();
        config.setApiId(apiConfig.getApiId());
        config.setInterfaceName(apiConfig.getInterfaceName());
        config.setMethodName(apiConfig.getMethodName());
        config.setBusiness(apiConfig.getBusiness());
        config.setVersion(apiConfig.getVersion());
        config.setNeedAuth(Boolean.TRUE.equals(apiConfig.getNeedAuth()));
        config.setPreAuthBeans(apiConfig.getPreAuthBeans());
        List<SystemGenericApiParamEntity> params = genericApiParamService.listByApiId(apiConfig.getApiId());
        config.setParams(params.stream().map(this::toParamConfig).toList());
        return config;
    }

    /**
     * 入参定义实体到中立模型的转换
     *
     * @param param 入参定义实体，不能为空
     * @return 入参定义
     */
    private GenericApiParamConfig toParamConfig(SystemGenericApiParamEntity param) {
        return new GenericApiParamConfig(param.getParamOrder(), param.getParamName(), param.getParamType(),
            Boolean.TRUE.equals(param.getRequiredFlag()), param.getDescription(), param.getExample());
    }
}
