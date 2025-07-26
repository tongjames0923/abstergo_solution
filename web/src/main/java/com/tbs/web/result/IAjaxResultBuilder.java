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
         * @param feild
         * @param value
         * @return
         */
        <T> IAjaxResult1Step appendField(String feild, T value);

        /**
         * 字段添加完成执行下一步
         *
         * @return
         */
        IAjaxResult2Step next();

    }

    public static interface IAjaxResult2Step {

        /**
         * 整理字段
         *
         * @param consumer
         * @return
         */
        IAjaxResult2Step preHandle(SConsumer<Map<String, Object>> consumer);

        /**
         * 构建结果
         *
         * @return
         */
        AjaxResult build();
    }

    /**
     * 创建一个结果生成器
     *
     * @return
     */
    IAjaxResult1Step create();
}
