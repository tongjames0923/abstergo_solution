package com.tbs.view.route.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.view.route.config.property.ViewRouteProperty;
import com.tbs.view.route.impl.YmlViewServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * 路由配置类
 *
 * @author tongj
 */
@Configuration
public class ViewRouteConfig {

    @Resource
    private ViewRouteProperty viewRouteProperty;

    @Bean
    @ConditionalOnMissingBean(IViewRouteService.class)
    public IViewRouteService getViewRouteService() {
        return BeanUtil.buildBeanFromProperties(YmlViewServiceImpl::new, viewRouteProperty.getViewRouteServiceClass(),
            BeanUtil::buildByEmptyArgs);
    }
}
