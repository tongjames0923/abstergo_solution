package com.tbs.logging.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * 默认日志上下文解析实现
 * <p>
 * 编排顺序：按来源定位（或按优先级顺序尝试）创建器 → 产出快照 →
 * 拦截器按优先级依次增强 → 返回。单个创建器或拦截器的异常被捕获并降级
 * （记 warn 后继续），保证日志采集链路不因上下文环节失败而中断。
 * </p>
 * <p>
 * 构造时按 {@link ILoggingContextCreator#getOrder()} 与
 * {@link ILoggingContextInterceptor#getOrder()} 升序定序；
 * 来源标识重复视为装配错误，保留先注册者并记 warn。
 * </p>
 *
 * @author abstergo
 * @see ILoggingContextResolver
 */
public class DefaultLoggingContextResolver implements ILoggingContextResolver {

    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(DefaultLoggingContextResolver.class);

    /**
     * 已按优先级升序排列的创建器
     */
    private final List<ILoggingContextCreator> creators;

    /**
     * 已按优先级升序排列的拦截器
     */
    private final List<ILoggingContextInterceptor> interceptors;

    /**
     * 来源标识到创建器的索引
     */
    private final Map<String, ILoggingContextCreator> creatorIndex = new HashMap<>();

    /**
     * 构造解析器
     *
     * @param creators     创建器集合，允许为 null 或空
     * @param interceptors 拦截器集合，允许为 null 或空
     */
    public DefaultLoggingContextResolver(List<ILoggingContextCreator> creators,
        List<ILoggingContextInterceptor> interceptors) {
        this.creators = sorted(creators, ILoggingContextCreator::getOrder);
        for (ILoggingContextCreator creator : this.creators) {
            ILoggingContextCreator previous = creatorIndex.putIfAbsent(creator.source(), creator);
            if (previous != null) {
                log.warn("日志上下文创建器来源标识重复, 保留先注册者: source={}, 保留={}, 忽略={}",
                    creator.source(), previous.getClass().getName(), creator.getClass().getName());
            }
        }
        this.interceptors = sorted(interceptors, ILoggingContextInterceptor::getOrder);
    }

    @Override
    public ILoggingContext resolve(String source) {
        if (source != null) {
            ILoggingContext context = tryCreate(creatorIndex.get(source));
            if (context != null) {
                return enhance(context);
            }
        }
        return DefaultLoggingContext.empty();
    }

    @Override
    public ILoggingContext resolveCurrent() {
        for (ILoggingContextCreator creator : creators) {
            ILoggingContext context = tryCreate(creator);
            if (context != null) {
                return enhance(context);
            }
        }
        return DefaultLoggingContext.empty();
    }

    /**
     * 尝试经创建器产出快照，失败降级为 null
     *
     * @param creator 创建器，允许为 null
     * @return 快照；创建器为 null、不适用或抛出异常时为 null
     */
    private ILoggingContext tryCreate(ILoggingContextCreator creator) {
        if (creator == null) {
            return null;
        }
        try {
            return creator.create().orElse(null);
        } catch (Exception e) {
            log.warn("日志上下文创建器执行失败(降级跳过): source={}", creator.source(), e);
            return null;
        }
    }

    /**
     * 拦截器管道增强，单个拦截器失败降级跳过
     *
     * @param context 待增强快照
     * @return 增强后的快照
     */
    private ILoggingContext enhance(ILoggingContext context) {
        for (ILoggingContextInterceptor interceptor : interceptors) {
            try {
                interceptor.intercept(context);
            } catch (Exception e) {
                log.warn("日志上下文拦截器执行失败(降级跳过): interceptor={}",
                    interceptor.getClass().getName(), e);
            }
        }
        return context;
    }

    /**
     * 按优先级升序排序（防御性拷贝）
     *
     * @param list  原始集合，允许为 null
     * @param order 优先级提取函数
     * @param <T>   元素类型
     * @return 排序后的新列表，保证不为 null
     */
    private static <T> List<T> sorted(List<T> list, ToIntFunction<T> order) {
        List<T> result = new ArrayList<>(list == null ? new ArrayList<>() : list);
        result.sort(Comparator.comparingInt(order));
        return result;
    }
}
