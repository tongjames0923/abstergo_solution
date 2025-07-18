package com.tbs.web.result.impl;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应结果处理
 *
 * @author tongj
 */
@ControllerAdvice
public class ResponseAjaxAspect implements ResponseBodyAdvice {

    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception e) {
        return AjaxUtil.getInstance().error(e.getMessage(), e);
    }


    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
        Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof AjaxResult) {
            return ((AjaxResult)body).getMap();
        }
        return body;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 仅处理 BaseController 或其子类的方法返回值为 AjaxResult 的情况
        Object bean = returnType.getContainingClass();
        boolean r = bean instanceof Class<?> &&
            BaseController.class.isAssignableFrom((Class<?>)bean) &&
            returnType.getParameterType().equals(AjaxResult.class);
        return r;
    }
}
