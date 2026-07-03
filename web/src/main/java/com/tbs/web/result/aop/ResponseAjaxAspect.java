package com.tbs.web.result.aop;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IResultCreator;
import com.tbs.web.result.annotation.NoWrapped;
import com.tbs.web.result.annotation.Wrapped;
import com.tbs.web.uitl.AjaxUtil;
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
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private IAjaxBuilderProvider provider;

    @Resource
    private HttpServletResponse response;

    @ExceptionHandler(Throwable.class)
    public AjaxResult handleException(Throwable e) throws Throwable {
        if (response.isCommitted()) {
            return null;
        }

        Throwable cause = AssertUtil.toFormattedRuntimeException(e).orElse(e);

        // 根据异常级别选择日志级别，降低业务异常的告警噪音
        if (cause instanceof IExceptionLevel) {
            ErrorLevelEnum level = ((IExceptionLevel) cause).getLevel();
            if (level == ErrorLevelEnum.USER_INPUT_ERROR || level == ErrorLevelEnum.BUSINESS_LOGIC_ERROR) {
                log.warn("业务异常: {}", cause.getMessage());
            } else {
                log.error("系统异常", cause);
            }
        } else {
            log.error("未知异常", cause);
        }

        return AjaxUtil.getInstance().createAjaxResultOnError(cause);
    }

    private static final Map<String, Boolean> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Wrapped> methodWrappedCache = new ConcurrentHashMap<>();

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
        String methodName = getMethodName(method);
        return methodCache.computeIfAbsent(methodName, k -> {
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
            // 缓存 @Wrapped 注解实例，供 beforeBodyWrite 使用
            methodWrappedCache.put(methodName, wrappeds.iterator().next());
            return provider.support(method);
        });

    }
}
