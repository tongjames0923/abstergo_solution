package com.tbs.view.route.config.property;

import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.impl.YmlViewServiceImpl;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "tbs.view.route")
/**
 * 视图路由配置属性类，用于加载和管理视图路由相关的配置属性
 * @author tongj
 */
@Component
@Data
public class ViewRouteProperty {
    /**
     * 视图路由服务类，默认为YmlViewServiceImpl
     */
    private Class<? extends IViewRouteService> viewRouteServiceClass = YmlViewServiceImpl.class;
}
