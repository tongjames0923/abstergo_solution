package com.tbs.cdc.bus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tbs.cdc.annotation.CdcListener;
import com.tbs.cdc.i18n.CdcI18nConstant;
import com.tbs.cdc.listener.CdcChangeListener;
import com.tbs.cdc.listener.CdcSubscription;
import com.tbs.cdc.model.CdcChangeType;
import com.tbs.cdc.model.CdcEvent;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import jakarta.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * CDC监听器注册路由表
 * <p>
 * 在全部单例初始化完成后（{@link SmartInitializingSingleton}）扫描两类监听风格并构建路由表：
 * 注解式{@link CdcListener}方法、编程式{@link CdcChangeListener} Bean。分发时按事件命中
 * 关注的库表与变更类型返回匹配监听器列表，路由本身线程安全（构建后只读）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "tbs.cdc", name = "enable", havingValue = "true", matchIfMissing = true)
public class CdcListenerRegistry implements SmartInitializingSingleton {

    @Resource
    private ApplicationContext applicationContext;

    /**
     * 路由表，启动期构建完成后只读
     */
    private volatile List<ListenerRoute> routes = Collections.emptyList();

    /**
     * 全部单例初始化完成后构建监听路由表
     */
    @Override
    public void afterSingletonsInstantiated() {
        List<ListenerRoute> built = new ArrayList<>();
        built.addAll(scanAnnotatedListeners());
        built.addAll(scanProgrammaticListeners());
        this.routes = Collections.unmodifiableList(built);
        log.info("CDC监听器路由表构建完成, 共{}个监听路由", this.routes.size());
    }

    /**
     * 匹配事件命中的监听路由
     *
     * @param event 变更事件，非null
     * @return 命中的监听路由列表，可能为空
     */
    public List<ListenerRoute> match(CdcEvent event) {
        List<ListenerRoute> matched = new ArrayList<>();
        for (ListenerRoute route : routes) {
            if (route.matches(event)) {
                matched.add(route);
            }
        }
        return matched;
    }

    /**
     * 扫描所有Bean中标注{@link CdcListener}的方法，构建注解式监听路由
     *
     * @return 注解式监听路由列表
     */
    private List<ListenerRoute> scanAnnotatedListeners() {
        List<ListenerRoute> result = new ArrayList<>();
        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class);
        for (Object bean : beans.values()) {
            Class<?> targetClass = ClassUtils.getUserClass(bean);
            Method[] methods = ReflectionUtils.getDeclaredMethods(targetClass);
            for (Method method : methods) {
                CdcListener annotation = AnnotatedElementUtils.findMergedAnnotation(method, CdcListener.class);
                if (annotation == null) {
                    continue;
                }
                ListenerRoute route = buildAnnotatedRoute(bean, targetClass, method, annotation);
                result.add(route);
                log.info("注册注解式CDC监听: {}#{}", targetClass.getSimpleName(), method.getName());
            }
        }
        return result;
    }

    /**
     * 由注解声明构建单个监听路由，校验方法签名须有且仅有一个{@link CdcEvent}入参
     *
     * @param bean       目标Bean实例
     * @param targetClass 用户类（去除代理）
     * @param method     监听方法
     * @param annotation 注解实例
     * @return 监听路由
     */
    private ListenerRoute buildAnnotatedRoute(Object bean, Class<?> targetClass, Method method,
        CdcListener annotation) {
        Class<?>[] paramTypes = method.getParameterTypes();
        boolean signatureValid = paramTypes.length == 1 && CdcEvent.class.isAssignableFrom(paramTypes[0]);
        AssertUtil.isTrue(!signatureValid).i18n(CdcI18nConstant.LISTENER_METHOD_INVALID,
            targetClass.getName() + "#" + method.getName()).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        ReflectionUtils.makeAccessible(method);
        Consumer<CdcEvent> invoker = event -> ReflectionUtils.invokeMethod(method, bean, event);
        CdcSubscription subscription = CdcSubscription.builder()
            .database(null)
            .tablePatterns(new LinkedHashSet<>(Arrays.asList(annotation.tables())))
            .changeTypes(toTypeSet(annotation.types()))
            .async(annotation.async())
            .build();
        return new ListenerRoute(subscription, invoker,
            targetClass.getSimpleName() + "#" + method.getName());
    }

    /**
     * 扫描所有编程式{@link CdcChangeListener} Bean，构建监听路由
     *
     * @return 编程式监听路由列表
     */
    private List<ListenerRoute> scanProgrammaticListeners() {
        List<ListenerRoute> result = new ArrayList<>();
        Map<String, CdcChangeListener> listeners = applicationContext.getBeansOfType(CdcChangeListener.class);
        for (Map.Entry<String, CdcChangeListener> entry : listeners.entrySet()) {
            CdcChangeListener listener = entry.getValue();
            CdcSubscription subscription = listener.getSubscription();
            AssertUtil.isTrue(subscription == null).i18n(CdcI18nConstant.SUBSCRIPTION_REQUIRED)
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
            result.add(new ListenerRoute(subscription, listener::onCdcEvent,
                "CdcChangeListener:" + entry.getKey()));
            log.info("注册编程式CDC监听: {}", entry.getKey());
        }
        return result;
    }

    /**
     * 将注解变更类型数组转为集合，空数组表示订阅全部类型
     *
     * @param types 注解声明的类型数组
     * @return 类型集合，空数组返回空集合
     */
    private Set<CdcChangeType> toTypeSet(CdcChangeType[] types) {
        if (types == null || types.length == 0) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(Arrays.asList(types));
    }

    /**
     * 单个监听路由：订阅条件 + 事件消费器 + 描述
     */
    public static final class ListenerRoute {

        /**
         * 订阅描述
         */
        private final CdcSubscription subscription;

        /**
         * 事件消费器
         */
        private final Consumer<CdcEvent> invoker;

        /**
         * 路由描述，用于日志
         */
        private final String description;

        /**
         * 预编译的表正则集合，避免匹配时重复编译
         */
        private final List<Pattern> compiledTablePatterns;

        /**
         * 构造监听路由
         *
         * @param subscription 订阅描述
         * @param invoker      事件消费器
         * @param description  路由描述
         */
        public ListenerRoute(CdcSubscription subscription, Consumer<CdcEvent> invoker, String description) {
            this.subscription = subscription;
            this.invoker = invoker;
            this.description = description;
            this.compiledTablePatterns = compilePatterns(subscription.getTablePatterns());
        }

        /**
         * 判断事件是否命中本路由的订阅范围
         *
         * @param event 变更事件
         * @return true表示命中
         */
        public boolean matches(CdcEvent event) {
            if (StrUtil.isNotBlank(subscription.getDatabase())
                && !subscription.getDatabase().equals(event.getDatabase())) {
                return false;
            }
            Set<CdcChangeType> types = subscription.getChangeTypes();
            if (CollUtil.isNotEmpty(types) && !types.contains(event.getChangeType())) {
                return false;
            }
            if (CollUtil.isEmpty(compiledTablePatterns)) {
                return true;
            }
            // 事件无表名（如快照/心跳/DDL）时不可能命中任何表模式，直接不匹配，避免向Pattern.matcher传入null
            String table = event.getTable();
            if (StrUtil.isBlank(table)) {
                return false;
            }
            String database = event.getDatabase();
            String qualified = StrUtil.isNotBlank(database) ? database + "." + table : table;
            for (Pattern pattern : compiledTablePatterns) {
                if (pattern.matcher(table).matches() || pattern.matcher(qualified).matches()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 执行监听消费
         *
         * @param event 变更事件
         */
        public void invoke(CdcEvent event) {
            invoker.accept(event);
        }

        /**
         * 是否异步执行
         *
         * @return true表示异步
         */
        public boolean isAsync() {
            return subscription.isAsync();
        }

        /**
         * 获取路由描述
         *
         * @return 描述文本
         */
        public String getDescription() {
            return description;
        }

        /**
         * 将表名模式集合预编译为正则，非法正则回退为字面量精确匹配
         *
         * @param patterns 表名/正则集合
         * @return 编译后的正则列表
         */
        private static List<Pattern> compilePatterns(Set<String> patterns) {
            if (CollUtil.isEmpty(patterns)) {
                return Collections.emptyList();
            }
            List<Pattern> compiled = new ArrayList<>(patterns.size());
            for (String regex : patterns) {
                try {
                    compiled.add(Pattern.compile(regex));
                } catch (Exception e) {
                    compiled.add(Pattern.compile(Pattern.quote(regex)));
                }
            }
            return compiled;
        }
    }
}
