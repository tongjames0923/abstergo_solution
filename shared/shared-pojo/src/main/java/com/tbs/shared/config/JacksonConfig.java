package com.tbs.shared.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tbs.shared.serialize.OrderedModelDeserializer;
import com.tbs.shared.serialize.OrderedModelSerializer;
import com.tbs.sql.order.impl.OrderedModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson配置类，用于注册自定义的序列化器和反序列化器
 */
@Configuration
public class JacksonConfig {

    @Bean
    public SimpleModule queryOrderModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(OrderedModel.class, new OrderedModelSerializer());
        module.addDeserializer(OrderedModel.class, new OrderedModelDeserializer());
        return module;
    }
}
