package com.tbs.common;

/**
 * 当Application 启动后运行
 *
 * @author abstergo
 */
public interface IStartup {
    /**
     * 运行方法
     *
     * @throws RuntimeException 可能得运行错误
     */
    void startUp() throws RuntimeException;

    /**
     * 是否可选,默认不可选,当可选时，且配置文件中配置了可选项时，启动时将运行否则不运行。当不可选时，将直接运行。
     *
     * @return 是否可选
     */
    default boolean isOptional() {
        return false;
    }

}
