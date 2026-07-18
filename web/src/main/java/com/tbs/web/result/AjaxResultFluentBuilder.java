package com.tbs.web.result;

import com.tbs.web.enums.WebMessageTypeEnum;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ajax结果流式构建器
 * <p>
 * 提供流式API来构建AjaxResult对象，支持链式调用
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * AjaxResult result = AjaxResultFluentBuilder.builder()
 *     .code(200)
 *     .message("操作成功")
 *     .data(entity)
 *     .redirect("/admin/user")
 *     .build();
 *
 * // 或使用快捷方法
 * AjaxResult result = AjaxResultFluentBuilder.builder()
 *     .success(entity)
 *     .redirect("/admin/user")
 *     .build();
 * </pre>
 * </p>
 *
 * @author Qoder
 */
public class AjaxResultFluentBuilder {

    /**
     * 存储所有字段的Map
     */
    private final Map<String, Object> map = new LinkedHashMap<>();

    /**
     * 私有构造方法
     */
    private AjaxResultFluentBuilder() {
    }

    /**
     * 创建流式构建器实例
     *
     * @return AjaxResultFluentBuilder实例
     */
    public static AjaxResultFluentBuilder builder() {
        return new AjaxResultFluentBuilder();
    }

    /**
     * 设置响应代码
     *
     * @param code 响应代码
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder code(int code) {
        map.put(AjaxResult.CODE_KEY, code);
        return this;
    }

    /**
     * 设置响应消息
     *
     * @param message 响应消息
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder message(String message) {
        map.put(AjaxResult.MESSAGE_KEY, message);
        return this;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder data(Object data) {
        map.put(AjaxResult.DATA_KEY, data);
        return this;
    }

    /**
     * 设置重定向路径
     *
     * @param redirect 重定向路径
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder redirect(String redirect) {
        map.put("redirect", redirect);
        return this;
    }

    /**
     * 设置消息类型（用于通知/错误提示）
     *
     * @param type 消息类型
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder type(WebMessageTypeEnum type) {
        if (type != null) {
            map.put("type", type.getValue());
        }
        return this;
    }

    /**
     * 设置提示持续时间（毫秒）
     *
     * @param duration 持续时间
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder duration(Integer duration) {
        if (duration != null && duration > 0) {
            map.put("duration", duration);
        }
        return this;
    }

    /**
     * 设置通知标题
     *
     * @param title 标题
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder title(String title) {
        if (title != null && !title.isEmpty()) {
            map.put("title", title);
        }
        return this;
    }

    /**
     * 添加自定义字段
     *
     * @param key   字段名
     * @param value 字段值
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder field(String key, Object value) {
        map.put(key, value);
        return this;
    }

    /**
     * 批量添加字段
     *
     * @param fields 字段Map
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder fields(Map<String, Object> fields) {
        if (fields != null) {
            map.putAll(fields);
        }
        return this;
    }

    /**
     * 配置为成功响应（仅设置code和message）
     *
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder success() {
        return code(AjaxResult.DEFAULT_SUCCESS_CODE).message(AjaxResult.getDefaultSuccessMessage());
    }

    /**
     * 配置为成功响应，包含数据
     *
     * @param data 响应数据
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder success(Object data) {
        return success().data(data);
    }

    /**
     * 配置为成功响应，包含消息和数据
     *
     * @param message 响应消息
     * @param data    响应数据
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder success(String message, Object data) {
        return code(AjaxResult.DEFAULT_SUCCESS_CODE).message(message).data(data);
    }

    /**
     * 配置为错误响应
     *
     * @param message 错误消息
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder error(String message) {
        return code(AjaxResult.DEFAULT_ERROR_CODE).message(message).type(WebMessageTypeEnum.ERROR).duration(10 * 1000);
    }

    /**
     * 配置为错误响应，包含数据
     *
     * @param message 错误消息
     * @param data    错误数据
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder error(String message, Object data) {
        return error(message).data(data);
    }

    /**
     * 配置为调试错误响应
     *
     * @param message 错误消息
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder debugError(String message) {
        return code(AjaxResult.DEBUG_ERROR_CODE).message(message);
    }

    /**
     * 配置为调试错误响应，包含数据
     *
     * @param message 错误消息
     * @param data    错误数据
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder debugError(String message, Object data) {
        return debugError(message).data(data);
    }

    /**
     * 配置为通知响应
     *
     * @param title   通知标题
     * @param message 通知消息
     * @param type    通知类型
     * @return 当前构建器实例
     */
    public AjaxResultFluentBuilder notice(String title, String message, WebMessageTypeEnum type) {
        return code(AjaxResult.NOTICE_CODE).title(title).message(message).type(type).duration(5 * 1000);
    }

    /**
     * 构建AjaxResult对象
     *
     * @return AjaxResult实例
     */
    public AjaxResult build() {
        AjaxResult result = new AjaxResult();
        result.setMap(new LinkedHashMap<>(map));
        return result;
    }

    /**
     * 构建并获取底层Map（用于兼容某些场景）
     *
     * @return 包含所有字段的Map
     */
    public Map<String, Object> buildMap() {
        return new LinkedHashMap<>(map);
    }
}
