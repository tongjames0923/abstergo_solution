package com.tbs.logging.core;

import com.tbs.common.context.IContext;

import java.util.Map;
import java.util.Optional;

/**
 * 日志上下文——一次日志采集动作的环境快照
 * <p>
 * 由 {@link ILoggingContextCreator} 按执行来源（web、Dubbo、定时任务等）适配创建，
 * 经 {@link ILoggingContextInterceptor} 管道增强后，由 {@link ILoggingContextApplier}
 * 回填至日志表单。本接口仅承载采集时刻的值快照，不绑定任何执行环境，
 * 字段缺失时以 {@link Optional#empty()} 降级表达。
 * </p>
 * <p>
 * 除标准访问器外，来源特有的附加信息经 {@link #setAttribute(String, Object)}
 * 扩展属性容器存取，避免本契约感知具体来源模块。
 * </p>
 *
 * @author tongj
 * @see ILoggingContextCreator
 * @see ILoggingContextInterceptor
 * @see ILoggingContextApplier
 */
public interface ILoggingContext extends IContext {

    /**
     * 获取链路追踪 ID
     *
     * @return 链路追踪 ID 的 Optional 包装，无来源数据时为 empty
     */
    Optional<String> getTraceId();

    /**
     * 获取操作用户名
     *
     * @return 操作用户名的 Optional 包装，无来源数据时为 empty
     */
    Optional<String> getOperateUser();

    /**
     * 获取操作来源 IP
     *
     * @return 来源 IP 的 Optional 包装，无来源数据时为 empty
     */
    Optional<String> getOperateIp();

    /**
     * 获取请求 URL
     *
     * @return 请求 URL 的 Optional 包装，无来源数据时为 empty
     */
    Optional<String> getRequestUrl();

    /**
     * 获取请求方法
     *
     * @return 请求方法的 Optional 包装，无来源数据时为 empty
     */
    Optional<String> getRequestMethod();

    /**
     * 获取采集开始时间戳
     * <p>
     * 未采集到来源的开始时间时，为快照创建时刻。
     * </p>
     *
     * @return 开始时间戳（毫秒）
     */
    long getStartTime();

    /**
     * 获取来源标识
     *
     * @return 来源标识，如 "web" / "dubbo" / "scheduled"；无来源数据时为 "default"
     */
    String source();

    /**
     * 设置扩展属性
     *
     * @param key   属性键
     * @param value 属性值，传入 null 将移除该属性
     */
    void setAttribute(String key, Object value);

    /**
     * 获取扩展属性并转换为指定类型
     *
     * @param key  属性键
     * @param type 期望的属性类型
     * @param <T>  属性类型
     * @return 类型安全属性值的 Optional 包装，属性不存在或类型不匹配时为 empty
     */
    <T> Optional<T> getAttribute(String key, Class<T> type);

    /**
     * 获取全部扩展属性的只读视图
     *
     * @return 扩展属性只读 Map，保证不为 null
     */
    Map<String, Object> getAttributes();
}
