package com.tbs.app.controller.publics;

import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.web.genric.AbstractGenricApiInstance;
import com.tbs.web.genric.AbstractNoAuthGenricApiController;
import com.tbs.web.genric.DubboGenricApiInstance;
import com.tbs.web.uitl.DubboUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 公开泛化调用接口控制器
 * <p>
 * 提供无需认证的泛化API调用功能，配置信息从数据库实体中获取
 * </p>
 * <p>
 * 本控制器属于旧泛化调用链路（仅承载 {@code POST /public/genric/invoke}），与匿名新入口
 * {@code POST /public/genric/call/{apiId}} 并行存活。
 * </p>
 *
 * @author abstergo
 * @deprecated 旧匿名信封入口。替代：{@link PublicGenricInvokeController}（参数定义取自 sys_generic_api_param，
 * 响应单层）；旧入口无调用量后的首个发布版本移除。
 */
@Deprecated
@Slf4j
@RestController
@RequestMapping("/public/genric")
public class PublicGenricController extends AbstractNoAuthGenricApiController {

    @Resource
    IGenericApiService genericApiService;

    @Resource
    private ApplicationConfig applicationConfig;

    @Resource
    private RegistryConfig registryConfig;

    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) {
        // 从数据库获取API配置，而不是从字典
        SystemGenericApiEntity apiConfig = genericApiService.getByApiId(id);
        AssertUtil.isTrue(apiConfig == null).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("generic.api.not.exists").params(id).throwIt();
        AssertUtil.isTrue(!Objects.equals(CommonFlagEnum.ACTIVE.getCode(), apiConfig.getFlag()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("generic.api.disabled").params(apiConfig.getApiName())
            .throwIt();

        AssertUtil.isTrue(Objects.equals(CommonFlagEnum.ACTIVE.getCode(), apiConfig.getNeedAuth()))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n("generic.api.need.auth").params(apiConfig.getApiName())
            .throwIt();
        // 通过服务发现获取实例地址
        var services = DubboUtils.getServiceInstance(apiConfig.getAppName());
        AssertUtil.isTrue(CollUtil.isEmpty(services)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("generic.api.service.instance.not.found").params(apiConfig.getAppName()).throwIt();

        List<AbstractGenricApiInstance> instances = new ArrayList<>();
        for (var service : services) {
            // 从预认证Bean名称字符串解析为数组
            String[] preAuthBeanNames = null;
            if (apiConfig.getPreAuthBeans() != null && !apiConfig.getPreAuthBeans().trim().isEmpty()) {
                preAuthBeanNames = apiConfig.getPreAuthBeans().split(",");
                // 去除每个Bean名称的前后空格
                for (int i = 0; i < preAuthBeanNames.length; i++) {
                    preAuthBeanNames[i] = preAuthBeanNames[i].trim();
                }
            }
            instances.add(new DubboGenricApiInstance(applicationConfig, registryConfig, id, apiConfig.getVersion(),
                apiConfig.getNeedAuth(),
                apiConfig.getInterfaceName(), apiConfig.getMethodName(), preAuthBeanNames));
        }
        return instances;
    }

    @Override
    public String getBusinessName() {
        return "公开泛化调用接口";
    }
}
