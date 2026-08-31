package com.tbs.web.result;

import com.tbs.common.function.SConsumer;

import java.util.Map;

/**
 * 请求结果生成接口
 *
 * @author tongj
 */
public interface IAjaxResultBuilder {

    public static interface IAjaxResult1Step {
        /**
         * 添加一个字段
         *
         * @param feild 字段名，不能为空
         * @param value 字段值，可以为 {@code null}
         * @param <T>   字段值类型
         * @return 当前步骤实例，用于链式追加更多字段
         */
        <T> IAjaxResult1Step appendField(String feild, T value);

        /**
         * 字段添加完成执行下一步
         *
         * @return 进入第二步（整理/构建阶段）
         */
        IAjaxResult2Step next();

    }

    public static interface IAjaxResult2Step {

        /**
         * 整理字段
         * <p>
         * 在构建前对底层字段 Map 做统一加工（如补充公共字段、改键名）。
         * </p>
         *
         * @param consumer 字段加工回调，不能为空
         * @return 当前步骤实例
         */
        IAjaxResult2Step preHandle(SConsumer<Map<String, Object>> consumer);

        /**
         * 构建结果
         *
         * @return 构建完成的 {@link AjaxResult}
         */
        AjaxResult build();
    }

    /**
     * 创建一个结果生成器
     *
     * @return 第一步构建器实例
     */
    IAjaxResult1Step create();
}
