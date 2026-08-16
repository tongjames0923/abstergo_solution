package com.tbs.web.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tbs.common.data.serializer.ToStringOrNumberDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson全局扩展，Long自动转字符串，无Bean冲突，完全继承spring.jackson.*配置
 */
@Configuration
public class JacksonCustomConfig implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        SimpleModule longToStringModule = new SimpleModule();
        // Long、基础long 序列化输出字符串，解决雪花ID前端精度丢失
        longToStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longToStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // 反序列化支持字符串和数字两种形式
        longToStringModule.addDeserializer(Long.class, new ToStringOrNumberDeserializer());
        longToStringModule.addDeserializer(Long.TYPE, new ToStringOrNumberDeserializer());

        jacksonObjectMapperBuilder.modules(longToStringModule);
    }
}
