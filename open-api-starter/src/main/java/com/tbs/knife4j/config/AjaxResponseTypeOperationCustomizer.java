package com.tbs.knife4j.config;

import com.tbs.knife4j.annotation.AjaxResponseType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 {@link AjaxResponseType} 注解的OpenAPI响应Schema自定义器
 * <p>
 * 分两个阶段工作：
 * <ol>
 *     <li>收集阶段：在SpringDoc构建Operation时，通过 {@link OperationCustomizer} 读取方法上的
 *         {@link AjaxResponseType} 注解，将 方法全限定名 → 注解 映射缓存起来</li>
 *     <li>应用阶段：在所有Operation构建完成后，通过 {@link AjaxResponseTypeOpenApiCustomizer}
 *         遍历OpenAPI Spec中的每个Operation，根据缓存的注解信息替换响应Schema</li>
 * </ol>
 * 这种两阶段设计避免了OperationCustomizer的修改被SpringDoc后续处理覆盖的问题。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see AjaxResponseType
 * @see AjaxResponseTypeOpenApiCustomizer
 */
@Component
public class AjaxResponseTypeOperationCustomizer implements OperationCustomizer {

    /**
     * 缓存：方法全限定名 → AjaxResponseType注解
     * <p>
     * 在OperationCustomizer阶段收集，在OpenApiCustomizer阶段使用
     * </p>
     */
    private final Map<String, AjaxResponseType> annotationCache = new ConcurrentHashMap<>();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        AjaxResponseType annotation = handlerMethod.getMethodAnnotation(AjaxResponseType.class);
        if (annotation == null) {
            return operation;
        }

        // 缓存注解，供OpenApiCustomizer阶段使用
        Method method = handlerMethod.getMethod();
        String key = method.getDeclaringClass().getName() + "." + method.getName();
        annotationCache.put(key, annotation);

        // 在Operation的extensions中设置方法全限定名，供OpenApiCustomizer阶段识别
        if (operation.getExtensions() == null) {
            operation.extensions(new java.util.HashMap<>());
        }
        operation.getExtensions().put("x-ajax-response-type-method", key);

        return operation;
    }

    /**
     * 获取方法全限定名对应的AjaxResponseType注解
     *
     * @param methodKey 方法全限定名（类名.方法名）
     * @return AjaxResponseType注解，不存在则返回null
     */
    public AjaxResponseType getAnnotation(String methodKey) {
        return annotationCache.get(methodKey);
    }

    /**
     * 构建data字段的Schema
     *
     * @param annotation AjaxResponseType注解
     * @return data字段的Schema
     */
    public Schema<?> buildDataSchema(AjaxResponseType annotation) {
        Class<?> valueType = annotation.value();
        Class<?>[] genericArgs = annotation.genericArgs();

        // void类型表示无data字段
        if (valueType == void.class || valueType == Void.class) {
            return null;
        }

        // 集合类型：List, Set, Collection → array schema
        if (isCollectionType(valueType)) {
            return buildCollectionSchema(genericArgs);
        }

        // Map类型 → map schema
        if (Map.class.isAssignableFrom(valueType)) {
            return buildMapSchema(genericArgs);
        }

        // 普通类型 → $ref schema
        return buildRefSchema(valueType);
    }

    /**
     * 构建Ajax标准响应包装Schema：{ code, message, data }
     *
     * @param dataSchema data字段的Schema，为null时表示无data字段
     * @return 包装后的完整响应Schema
     */
    public Schema<?> buildWrapperSchema(Schema<?> dataSchema) {
        Map<String, Schema<?>> properties = new LinkedHashMap<>();

        // code字段
        Schema<?> codeSchema = new Schema<>();
        codeSchema.type("integer");
        codeSchema.description("响应状态码");
        codeSchema.example(200);
        properties.put("code", codeSchema);

        // message字段
        Schema<?> messageSchema = new StringSchema();
        messageSchema.setDescription("响应消息");
        properties.put("message", messageSchema);

        // data字段（可选）
        if (dataSchema != null) {
            dataSchema.setDescription("响应数据");
            properties.put("data", dataSchema);
        }

        Schema<?> wrapperSchema = new Schema<>();
        wrapperSchema.type("object");
        @SuppressWarnings("unchecked")
        Map<String, Schema> rawProperties = (Map<String, Schema>)(Map<String, ?>)properties;
        wrapperSchema.setProperties(rawProperties);
        return wrapperSchema;
    }

    /**
     * 将Schema替换到Operation的指定响应码中
     *
     * @param operation     OpenAPI操作对象
     * @param wrapperSchema 包装后的响应Schema
     */
    public void replaceResponseSchema(Operation operation, Schema<?> wrapperSchema) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        ApiResponse response = responses.get("200");
        if (response == null) {
            response = new ApiResponse().description("成功");
            responses.addApiResponse("200", response);
        }

        // 保留原有的description，只替换schema
        Content content = response.getContent();
        if (content == null) {
            content = new Content();
            response.setContent(content);
        }

        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = new MediaType();
            content.addMediaType("application/json", mediaType);
        }

        mediaType.setSchema(wrapperSchema);
    }

    /**
     * 构建集合类型的array Schema
     */
    private Schema<?> buildCollectionSchema(Class<?>[] genericArgs) {
        ArraySchema arraySchema = new ArraySchema();
        if (genericArgs != null && genericArgs.length > 0) {
            arraySchema.setItems(buildRefSchema(genericArgs[0]));
        } else {
            arraySchema.setItems(new Schema<>().type("object"));
        }
        return arraySchema;
    }

    /**
     * 构建Map类型的Schema
     */
    private Schema<?> buildMapSchema(Class<?>[] genericArgs) {
        MapSchema mapSchema = new MapSchema();
        if (genericArgs != null && genericArgs.length > 1) {
            mapSchema.additionalProperties(buildRefSchema(genericArgs[1]));
        } else {
            mapSchema.additionalProperties(new Schema<>().type("object"));
        }
        return mapSchema;
    }

    /**
     * 构建引用类型Schema，指向components/schemas中已注册的定义
     */
    private Schema<?> buildRefSchema(Class<?> clazz) {
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/" + clazz.getSimpleName());
        return schema;
    }

    /**
     * 判断是否为集合类型
     */
    private boolean isCollectionType(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz)
            || clazz == List.class
            || clazz == java.util.Set.class;
    }
}
