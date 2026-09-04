package com.tbs.view.route.constant;

/**
 * 视图路由模块国际化常量
 * <p>
 * 集中收拢 view-route 模块中面向用户的 i18n 资源 key，避免调用方散落硬编码字符串魔法值。 key 对应资源文件见
 * {@code view-route-default-impl/src/main/resources/i18n/messages-view-route.properties}（中文） 与
 * {@code messages-view-route_en_US.properties}（英文），新增 key 须中英文两份同步维护。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class ViewRouteI18nConstant {

    private ViewRouteI18nConstant() {
    }

    /* ===================== 路由配置 ===================== */

    /**
     * 路由配置为空校验提示：路由配置不能为空
     */
    public static final String CONFIG_CANNOT_BE_EMPTY = "view.route.config.cannot.be.empty";

    /**
     * 保存路由配置失败提示：保存路由配置失败
     */
    public static final String SAVE_FAILED = "view.route.save.failed";

    /**
     * 删除路由配置失败提示：删除路由配置失败
     */
    public static final String DELETE_FAILED = "view.route.delete.failed";

    /**
     * 从数据库加载路由配置失败提示：从数据库加载路由配置失败
     */
    public static final String LOAD_FAILED = "view.route.load.failed";

    /**
     * 功能尚未实现提示：功能尚未实现
     */
    public static final String FUNCTION_NOT_IMPLEMENTED = "view.route.function.not.implemented";

    /**
     * 加载路由配置文件失败提示：加载路由配置失败
     */
    public static final String LOAD_YAML_FAILED = "view.route.load.yaml.failed";

    /* ===================== 版本控制 ===================== */

    /**
     * 清空分组操作说明：清空分组
     */
    public static final String CLEAR_GROUP = "view.route.clear.group";

    /**
     * 目标版本不存在或不属于分组提示：目标版本不存在或不属于该分组
     */
    public static final String VERSION_NOT_FOUND_OR_NOT_IN_GROUP = "view.route.version.not.found.or.not.in.group";

    /**
     * 回滚来源版本记录：回滚自 v{0}
     */
    public static final String ROLLBACK_FROM = "view.route.rollback.from";

    /**
     * 回滚路由配置失败提示：回滚路由配置失败
     */
    public static final String ROLLBACK_FAILED = "view.route.rollback.failed";
}
