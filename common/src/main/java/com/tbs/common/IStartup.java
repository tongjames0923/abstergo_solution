package com.tbs.common;

/**
 * 应用启动后执行的接口
 * <p>
 * 该接口定义了在应用启动后需要执行的任务。实现此接口的类将在Spring容器初始化完成后自动执行startUp方法。
 * 可以用于执行初始化操作、数据加载、资源准备等任务。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IStartup {
    /**
     * 启动执行方法
     * <p>
     * 应用启动后需要执行的具体任务逻辑
     * </p>
     *
     * @throws RuntimeException 执行过程中可能抛出的运行时异常
     */
    void startUp() throws RuntimeException;

    /**
     * 是否为可选任务
     * <p>
     * 默认不可选，当设置为可选时，只有在配置文件中配置了相应选项时才会执行。
     * 不可选的任务将在应用启动时直接执行。
     * </p>
     *
     * @return true表示可选任务，false表示必选任务
     */
    default boolean isOptional() {
        return false;
    }

}
