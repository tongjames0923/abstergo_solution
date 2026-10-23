package com.tbs.common.provider;

/**
 * 路由元信息自声明钩子。
 * <p>
 * 供业务实现类自声明其 {@link SourceCoordinate} 的 business/version，仅当调用方持有真实对象时读取
 * （Spring Bean、进程内第三方 provider）。远程来源（Dubbo）一律从注册 URL 读 group/version，
 * 绝不调用本接口方法，避免在消费者代理上触发一次 RPC。
 * </p>
 * <p>
 * 业务接口不强制 implements 本接口：未实现时由来源按默认规则推导（Spring 取实现类简单名作 business、version 为空）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRouteMeta {

    /**
     * 自声明业务分区，留空则来源按默认规则（Spring 取实现类简单名）推导
     *
     * @return 业务分区，可为空串
     */
    default String business() {
        return "";
    }

    /**
     * 自声明发布/灰度版本，可为空
     *
     * @return 版本，可为空串
     */
    default String version() {
        return "";
    }
}
