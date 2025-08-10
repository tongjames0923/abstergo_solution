package com.tbs.app.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.model.QueryOrderedModel;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * QueryOrderedModel 序列化器
 * @author tongj
 */
public class QueryOrderedModelSerializer extends JsonSerializer<QueryOrderedModel> {
    @Override
    public void serialize(QueryOrderedModel value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.getOrdered() != null) {
            gen.writeString(value.getOrdered().name().toLowerCase());
        } else {
            gen.writeNull();
        }
    }
}
