package com.tbs.common;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;

import java.util.Objects;

/**
 * 系统功能定义
 * <p>
 * 用于标识系统中可支持的功能模块。每个功能模块对应一个自动注册器，
 * 当对应的包被扫描并注册时，该功能即被视为已启用。
 * </p>
 * <p>
 * 该类支持动态创建，用户可以根据需要定义新的功能模块：
 * <pre>
 * // 创建自定义功能
 * Feature myFeature = new Feature("my-module", "我的模块");
 *
 * // 使用预定义功能
 * Feature redis = Feature.REDIS;
 * </pre>
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class Feature {

    // ==================== 预定义功能模块 ====================

    /**
     * 通用功能 - 基础通用组件
     */
    public static final Feature COMMON = new Feature("com.tbs.common", "通用功能");

    /**
     * Redis缓存功能
     */
    public static final Feature REDIS = new Feature("com.tbs.redis", "Redis缓存");

    /**
     * RabbitMQ消息队列功能
     */
    public static final Feature RABBITMQ = new Feature("com.tbs.rabbitmq", "RabbitMQ消息队列");

    /**
     * 缓存管理功能
     */
    public static final Feature CACHE = new Feature("com.tbs.cache", "缓存管理");

    /**
     * 异步处理功能
     */
    public static final Feature ASYNC = new Feature("com.tbs.async", "异步处理");

    /**
     * 脚本引擎功能
     */
    public static final Feature SCRIPT = new Feature("com.tbs.script", "脚本引擎");

    /**
     * 动态视图路由功能
     */
    public static final Feature VIEW_ROUTE = new Feature("com.tbs.view-route", "动态视图路由");

    /**
     * Web基础组件功能
     */
    public static final Feature WEB = new Feature("com.tbs.web", "Web基础组件");

    /**
     * 共享业务库功能
     */
    public static final Feature SHARED = new Feature("com.tbs.shared", "共享业务库");

    /**
     * 闭包表树形结构功能
     */
    public static final Feature CLOSURE_TABLE = new Feature("com.tbs.closure-table", "闭包表树形结构");

    /**
     * API文档功能
     */
    public static final Feature KNIFE4J = new Feature("com.tbs.knife4j", "API文档");

    /**
     * 分布式任务调度功能
     */
    public static final Feature XXL_JOB = new Feature("com.tbs.xxl-job", "分布式任务调度");

    /**
     * 日志管理功能
     */
    public static final Feature LOGGING = new Feature("com.tbs.logging", "日志管理");

    /**
     * 链路追踪功能
     */
    public static final Feature TRACE = Feature.create("com.tbs.trace", "链路追踪");

    /**
     * 对象存储功能
     */
    public static final Feature OSS = Feature.create("com.tbs.oss", "对象存储");

    /**
     * 数据库变更数据捕获（CDC）功能
     */
    public static final Feature CDC = Feature.create("com.tbs.cdc", "数据库变更数据捕获");


    // ==================== 实例字段 ====================

    /**
     * 功能标识
     * <p>用于唯一标识一个功能模块，全局唯一</p>
     */
    private final String id;

    /**
     * 功能描述
     * <p>功能模块的中文描述，便于理解和展示</p>
     */
    private final String description;

    // ==================== 构造方法 ====================

    /**
     * 创建功能定义
     *
     * @param id          功能标识，不能为空
     * @param description 功能描述，不能为空
     * @throws IllegalArgumentException 当id或description为空时抛出
     */
    public Feature(String id, String description) {
        if (id == null || id.isEmpty()) {
            AssertUtil.throwError(() ->
                new IllegalArgumentException(I18nUtil.get("common.feature.id.required")));
        }
        if (description == null || description.isEmpty()) {
            AssertUtil.throwError(() ->
                new IllegalArgumentException(I18nUtil.get("common.feature.description.required")));
        }
        this.id = id;
        this.description = description;
    }

    // ==================== 实例方法 ====================

    /**
     * 获取功能标识
     *
     * @return 功能标识字符串
     */
    public String getId() {
        return id;
    }

    /**
     * 获取功能描述
     *
     * @return 功能描述字符串
     */
    public String getDescription() {
        return description;
    }

    // ==================== Object方法重写 ====================

    /**
     * 比较两个Feature是否相同
     * <p>基于id进行比较，id相同即视为相同功能</p>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof Feature) {
            return id.equals(((Feature) o).id);
        }
        return false;
    }

    /**
     * 基于id计算hashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 返回功能的字符串表示
     */
    @Override
    public String toString() {
        return "Feature{id='" + id + "', description='" + description + "'}";
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 根据功能标识查找预定义的功能
     * <p>
     * 支持大小写不敏感的查找，如果未找到返回null
     * </p>
     *
     * @param id 功能标识
     * @return 对应的Feature，未找到返回null
     */
    public static Feature of(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        // 通过反射获取所有预定义的静态常量
        for (Feature feature : new Feature[]{COMMON, REDIS, RABBITMQ, CACHE, ASYNC, SCRIPT,
                VIEW_ROUTE, WEB, SHARED, CLOSURE_TABLE, KNIFE4J, XXL_JOB, LOGGING, OSS, CDC}) {
            if (feature.id.equalsIgnoreCase(id)) {
                return feature;
            }
        }
        return null;
    }

    /**
     * 创建新的功能定义
     * <p>
     * 静态工厂方法，便于创建自定义功能模块
     * </p>
     *
     * @param id          功能标识
     * @param description 功能描述
     * @return 新的Feature实例
     */
    public static Feature create(String id, String description) {
        return new Feature(id, description);
    }
}
