package com.tbs.shared.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.tbs.sql.order.impl.OrderedModel;

import java.io.IOException;

/**
 * OrderedModel 序列化器
 * @author tongj
 */
public class OrderedModelSerializer extends JsonSerializer<OrderedModel> {
    @Override
    public void serialize(OrderedModel value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.getOrdered() != null) {
            gen.writeString(value.getOrdered().name().toLowerCase());
        } else {
            gen.writeNull();
        }
    }
}
