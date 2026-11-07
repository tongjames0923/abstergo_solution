package com.tbs.app.controller.privates.dev;

import com.tbs.app.service.GenericApiConfigLoader;
import com.tbs.web.genric.AbstractGenricInvokeController;
import com.tbs.web.genric.config.GenericApiConfig;
import com.tbs.web.uitl.AuthUtil;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 配置驱动的泛化调用控制器（需认证）
 * <p>
 * 新版链路入口，与旧的信封式 {@code POST /private/genric/invoke} 并存：
 * {@code POST /private/genric/call/{apiId}} 按登记的入参定义调用任意已发布的 Dubbo 方法，
 * {@code GET /private/genric/meta/{apiId}} 透出登记的入参定义。鉴权沿用网关能力：URL 分区要求登录态，
 * 细粒度权限由配置行的 {@code pre_auth_beans} 走预认证链。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/genric")
@PreAuthorize("@preAuthUtils.accountActived")
public class GenricInvokeController extends AbstractGenricInvokeController {

    @Resource
    private GenericApiConfigLoader genericApiConfigLoader;

    @Override
    protected GenericApiConfig loadApiConfig(String apiId) {
        return genericApiConfigLoader.loadInvokable(apiId);
    }

    @Override
    protected UserDetails getCurrentUser() {
        return AuthUtil.getInstance().getCurrentUser();
    }

    @Override
    public String getBusinessName() {
        return "泛化调用接口";
    }
}
