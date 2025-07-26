package com.tbs.web.result.aop;

import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.result.*;
import com.tbs.web.result.annotation.NoWrapped;
import com.tbs.web.result.annotation.Wrapped;
import com.tbs.web.result.impl.SimpleBuilderProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 响应结果处理
 *
 * @author tongj
 */
@Slf4j
@RestControllerAdvice
public class ResponseAjaxAspect implements ResponseBodyAdvice {

    @Resource
    private IOnErrorAjaxProvider onErrorAjaxProvider;

    @Resource
    private IAjaxBuilderProvider provider;

    @ExceptionHandler(Throwable.class)
    public AjaxResult handleException(Throwable e) {
        return onErrorAjaxProvider.onError(e);
    }

    private static final Map<String, Boolean> methodCache = new java.util.HashMap<>();
    private static final Map<String, Wrapped> methodWrappedCache = new java.util.HashMap<>();

    private String getMethodName(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
        Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof AjaxResult) {
            return ((AjaxResult)body).getMap();
        }
        if (AnnotatedElementUtils.hasAnnotation(returnType.getMethod(), NoWrapped.class)) {
            return body;
        }
        String methodName = getMethodName(returnType.getMethod());
        Wrapped wrapped = methodWrappedCache.get(methodName);
        if (wrapped == null) {
            return ((BaseController)SpringUtil.getBean(returnType.getContainingClass())).successResult(body).getMap();
        }
        IResultCreator resultCreator = SpringUtil.getBeanSafe(wrapped.value(),
            throwable -> log.error("处理结果创建器 {} 错误.Msg:{}", wrapped.value().getName(),
                throwable.getLocalizedMessage()));
        if (resultCreator == null) {
            return body;
        }
        return resultCreator.create(provider.getAjaxResultBuilder(), body).build().getMap();
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        Method method = returnType.getMethod();
        return methodCache.computeIfAbsent(getMethodName(method), k -> {
            if (method.getReturnType().equals(AjaxResult.class)) {
                return true;
            }
            if (AnnotatedElementUtils.hasAnnotation(method, NoWrapped.class)) {
                return false;
            }
            if (BeanUtil.isBaseFrom(method.getDeclaringClass(), BaseController.class)) {
                return true;
            }
            Collection<Wrapped> wrappeds =
                AnnotatedElementUtils.findAllMergedAnnotations(method, Wrapped.class).stream().filter((w) -> w != null)
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(wrappeds)) {
                return false;
            }
            return provider.support(method);
        });

    }
}
