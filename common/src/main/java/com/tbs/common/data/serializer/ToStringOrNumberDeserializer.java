package com.tbs.common.data.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Long类型反序列化器，支持字符串和数字两种输入格式
 *
 * 用于兼容前端传递的雪花ID（字符串或数字形式）
 * @author abstergo
 */
public class ToStringOrNumberDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            // 字符串形式："123456789"
            String text = p.getText().trim();
            if (text.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException e) {
                throw new IOException("Cannot parse Long from string: " + text, e);
            }
        } else if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
            // 数字形式：123456789
            return p.getValueAsLong();
        } else if (p.getCurrentToken() == JsonToken.START_OBJECT || p.getCurrentToken() == JsonToken.FIELD_NAME) {
            // JSON对象或其他复杂结构，返回null
            return null;
        }
        return null;
    }
}
