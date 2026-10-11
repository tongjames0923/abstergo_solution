package com.tbs.knife4j.config;

import com.tbs.knife4j.annotation.AjaxResponseType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 基于 {@link AjaxResponseType} 注解的OpenAPI Spec后处理自定义器
 * <p>
 * 在SpringDoc完成所有Operation构建后，遍历OpenAPI Spec中的每个Operation，
 * 通过方法全限定名查找 {@link AjaxResponseTypeOperationCustomizer} 中缓存的注解信息，
 * 动态构建并替换响应Schema。
 * </p>
 * <p>
 * 两阶段设计的原因：
 * {@link org.springdoc.core.customizers.OperationCustomizer} 在Operation构建过程中被调用，
 * 但SpringDoc后续会用自己的逻辑重新设置response content，导致OperationCustomizer的修改被覆盖。
 * 而 {@link OpenApiCustomizer} 在所有Operation构建完成后执行，修改不会被覆盖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see AjaxResponseType
 * @see AjaxResponseTypeOperationCustomizer
 */
@Slf4j
@Component
public class AjaxResponseTypeOpenApiCustomizer implements OpenApiCustomizer {

    private final AjaxResponseTypeOperationCustomizer operationCollector;

    public AjaxResponseTypeOpenApiCustomizer(AjaxResponseTypeOperationCustomizer operationCollector) {
        this.operationCollector = operationCollector;
    }

    @Override
    public void customise(OpenAPI openAPI) {
        if (openAPI.getPaths() == null) {
            return;
        }

        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            PathItem pathItem = pathEntry.getValue();
            processOperation(pathItem.getGet());
            processOperation(pathItem.getPost());
            processOperation(pathItem.getPut());
            processOperation(pathItem.getDelete());
            processOperation(pathItem.getPatch());
        }
    }

    /**
     * 处理单个Operation，如果包含 @AjaxResponseType 注解则替换其响应Schema
     *
     * @param operation 待处理的Operation
     */
    private void processOperation(Operation operation) {
        if (operation == null || operation.getExtensions() == null) {
            return;
        }

        // 从Operation的extensions中获取方法全限定名（由OperationCustomizer阶段设置）
        String methodKey = (String)operation.getExtensions().get("x-ajax-response-type-method");
        if (methodKey == null) {
            return;
        }

        AjaxResponseType annotation = operationCollector.getAnnotation(methodKey);
        if (annotation == null) {
            return;
        }

        // 构建data的Schema
        Schema<?> dataSchema = operationCollector.buildDataSchema(annotation);

        // 构建完整的Ajax响应包装Schema
        Schema<?> wrapperSchema = operationCollector.buildWrapperSchema(dataSchema);

        // 替换200响应的Schema
        operationCollector.replaceResponseSchema(operation, wrapperSchema);
    }
}
