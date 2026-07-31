package com.tbs.app.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.stereotype.Component;

@Component
public class OrderModelCustomJacksonConfig implements Jackson2ObjectMapperBuilderCustomizer {
    @Override
    public void customize(
        org.springframework.http.converter.json.Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        jacksonObjectMapperBuilder.serializerByType(com.tbs.sql.order.impl.OrderedModel.class,
            new com.tbs.shared.serialize.OrderedModelSerializer());
        jacksonObjectMapperBuilder.deserializerByType(com.tbs.sql.order.impl.OrderedModel.class,
            new com.tbs.shared.serialize.OrderedModelDeserializer());
    }
}
