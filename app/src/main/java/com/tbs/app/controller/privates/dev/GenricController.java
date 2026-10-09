package com.tbs.app.controller.privates.dev;

import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.enums.CommonFlagEnum;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.web.genric.AbstractAuthGenricApiController;
import com.tbs.web.genric.AbstractGenricApiInstance;
import com.tbs.web.genric.DubboGenricApiInstance;
import com.tbs.web.uitl.DubboUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 泛化接口控制器（需要认证）
 * <p>
 * 提供需要认证的泛化API调用功能，配置信息从数据库实体中获取
 * </p>
 *
 * @author abstergo
 */
@Slf4j
@RestController
@RequestMapping("/private/genric")
@PreAuthorize("@preAuthUtils.accountActived")
public class GenricController extends AbstractAuthGenricApiController {

    @Resource
    IGenericApiService genericApiService;

    @Resource
    private ApplicationConfig applicationConfig;

    @Resource
    private RegistryConfig registryConfig;

    @Override
    public String getBusinessName() {
        return "泛化调用接口";
    }

    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) {
        // 从数据库获取API配置，而不是从字典
        SystemGenericApiEntity apiConfig = genericApiService.getByApiId(id);
        AssertUtil.isTrue(apiConfig == null).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("generic.api.not.exists").params(id).throwIt();
        AssertUtil.isTrue(!Objects.equals(CommonFlagEnum.ACTIVE.getCode(), apiConfig.getFlag()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("generic.api.disabled").params(apiConfig.getApiName())
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
}
