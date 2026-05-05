package com.tbs.web.enums;

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
