package com.tbs.oss.config;

import com.tbs.common.AbstractAutoBeanRegister;
import com.tbs.common.Feature;

/**
 * OSS组件自动注册类
 * <p>
 * 继承自AbstractAutoBeanRegister，用于自动扫描和注册OSS相关组件，
 * 包括oss-api中的配置类以及各实现模块（如oss-minio-impl）中位于
 * com.tbs.oss子包下的工厂Bean
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class OssPackAutoRegister extends AbstractAutoBeanRegister {
    /**
     * 获取包名称
     * <p>
     * 返回OSS组件包的名称，用于日志和标识
     * </p>
     *
     * @return OSS包名称
     */
    @Override
    protected String getPackName() {
        return "OSS Pack";
    }

    /**
     * 获取当前注册器对应的功能模块
     *
     * @return OSS功能模块
     */
    @Override
    protected Feature getFeature() {
        return Feature.OSS;
    }

    /**
     * 获取基础扫描包路径
     * <p>
     * 返回需要扫描的OSS相关组件的包路径数组
     * </p>
     *
     * @return 基础扫描包路径数组
     */
    @Override
    protected String[] getScanningPackages() {
        return new String[] {"com.tbs.oss"};
    }
}
