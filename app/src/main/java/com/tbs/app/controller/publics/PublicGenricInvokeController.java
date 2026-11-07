package com.tbs.app.controller.publics;

import com.tbs.app.service.GenericApiConfigLoader;
import com.tbs.web.genric.AbstractGenricInvokeController;
import com.tbs.web.genric.config.GenericApiConfig;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置驱动的泛化调用控制器（免认证）
 * <p>
 * 匿名入口，与旧的 {@code POST /public/genric/invoke} 并存。登记为需要认证的 API 不允许走本入口，
 * 该约束由 Service 层断言保证；当前用户恒为 {@code null}，因此 {@code need_auth=true} 的配置在实例编排里也会被拦截。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/genric")
public class PublicGenricInvokeController extends AbstractGenricInvokeController {

    @Resource
    private GenericApiConfigLoader genericApiConfigLoader;

    @Override
    protected GenericApiConfig loadApiConfig(String apiId) {
        return genericApiConfigLoader.loadPublicInvokable(apiId);
    }

    @Override
    protected UserDetails getCurrentUser() {
        return null;
    }

    @Override
    public String getBusinessName() {
        return "公开泛化调用接口";
    }
}
