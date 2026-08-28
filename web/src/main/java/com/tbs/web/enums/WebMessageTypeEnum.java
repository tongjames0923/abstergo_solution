package com.tbs.web.enums;

/**
 * Web 消息类型枚举
 * <p>
 * 标识前端提示消息的展示形态（成功/错误/警告/信息），由 {@code AjaxUtil} 在构建响应时携带，
 * 供前端选择对应的 UI 样式。
 * </p>
 */
public enum WebMessageTypeEnum {

    SUCCESS("success"), ERROR("error"),

    WARNING("warning"), INFO("info");

    private String value;

    WebMessageTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
