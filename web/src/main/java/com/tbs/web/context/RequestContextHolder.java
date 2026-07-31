package com.tbs.web.context;

import java.util.Optional;

/**
 * 请求上下文本地持有者
 * <p>
 * 提供线程级别的请求上下文存储和访问能力。每个请求线程持有独立的 {@link RequestDataContext} 实例。
 * <p>
 * 本类仅负责当前服务实例内的上下文存储。跨服务的上下文传播由
 * {@link com.tbs.web.context.propagator.DubboRequestContextFilter} 等传播器自动完成。
 * <p>
 * 典型使用场景：
 * <pre>{@code
 * // 1. 在任何业务代码中获取当前请求上下文
 * RequestDataContext ctx = RequestContextHolder.current();
 *
 * // 2. 安全获取（可能为null）
 * RequestContextHolder.context().ifPresent(ctx -> {
 *     ctx.put("orderNo", orderNo);
 * });
 *
 * // 3. 获取或创建（适用于不确定上下文是否已初始化的场景）
 * RequestDataContext ctx = RequestContextHolder.getOrCreate();
 *
 * // 4. 获取traceId用于日志
 * String traceId = RequestContextHolder.traceId().orElse("unknown");
 * }</pre>
 *
 * @author abstergo
 * @see RequestDataContext
 */
public final class RequestContextHolder {

    /**
     * 使用InheritableThreadLocal支持子线程继承父线程的上下文
     * <p>
     * 在异步任务（如线程池）场景下，子线程可以继承父线程的请求上下文
     */
    private static final InheritableThreadLocal<RequestDataContext> CONTEXT_HOLDER = new InheritableThreadLocal<>();

    private RequestContextHolder() {
        // 工具类，禁止实例化
    }

    /**
     * 设置当前线程的请求上下文
     *
     * @param context 请求上下文，传入null将清除当前线程的上下文
     */
    public static void set(RequestDataContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(context);
        }
    }

    /**
     * 获取当前线程的请求上下文
     *
     * @return 当前线程的请求上下文，可能为null
     */
    public static RequestDataContext get() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 获取当前线程的请求上下文（Optional包装）
     *
     * @return 当前线程的请求上下文的Optional包装
     */
    public static Optional<RequestDataContext> context() {
        return Optional.ofNullable(CONTEXT_HOLDER.get());
    }

    /**
     * 获取当前线程的请求上下文，如果不存在则创建一个新的
     *
     * @return 当前线程的请求上下文，保证不为null
     */
    public static RequestDataContext getOrCreate() {
        RequestDataContext ctx = CONTEXT_HOLDER.get();
        if (ctx == null) {
            ctx = RequestDataContext.create();
            CONTEXT_HOLDER.set(ctx);
        }
        return ctx;
    }

    /**
     * 获取当前请求上下文的便捷方法（等同于 {@link #get()}）
     * <p>
     * 适用于在业务代码中直接获取上下文的场景，如：
     * <pre>{@code
     * String traceId = RequestContextHolder.current().getTraceId();
     * }</pre>
     *
     * @return 当前线程的请求上下文，可能为null
     */
    public static RequestDataContext current() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 获取当前请求的traceId
     *
     * @return traceId的Optional包装
     */
    public static Optional<String> traceId() {
        return context().map(RequestDataContext::getTraceId);
    }

    /**
     * 向当前请求上下文的业务数据Map中放入一个键值对
     * <p>
     * 便捷方法，等同于 {@code current().put(key, value)}
     *
     * @param key   业务标识
     * @param value 业务数据
     */
    public static void put(String key, Object value) {
        RequestDataContext ctx = CONTEXT_HOLDER.get();
        if (ctx != null) {
            ctx.put(key, value);
        }
    }

    /**
     * 从当前请求上下文的业务数据Map中获取值
     * <p>
     * 便捷方法，等同于 {@code context().flatMap(ctx -> ctx.get(key))}
     *
     * @param key 业务标识
     * @return 值的Optional包装
     */
    public static Optional<Object> get(String key) {
        return context().flatMap(ctx -> ctx.get(key));
    }

    /**
     * 从当前请求上下文的业务数据Map中获取指定类型的值
     * <p>
     * 便捷方法，支持类型安全的获取
     *
     * @param key  业务标识
     * @param type 期望的值类型
     * @param <T>  泛型类型
     * @return 类型安全的值的Optional包装
     */
    public static <T> Optional<T> get(String key, Class<T> type) {
        return context().flatMap(ctx -> ctx.get(key, type));
    }

    /**
     * 清除当前线程的请求上下文
     * <p>
     * 在请求结束时调用，防止内存泄漏
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
