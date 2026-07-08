package com.tbs.web.uitl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.IStartup;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.Comparators;
import com.tbs.web.enums.WebMessageTypeEnum;
import com.tbs.web.result.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Ajax工具类，用于构建统一的Ajax响应结果 提供了成功、失败和错误等不同情况下的响应结果构建方法
 *
 * @author tongj
 */
@Component
public class AjaxUtil implements IStartup {

    /**
     * IAjaxBuilderProvider实例，用于创建AjaxResult对象
     */
    @Resource
    private IAjaxBuilderProvider ajaxBuilderProvider;

    private static volatile Collection<IOnErrorAjaxProvider> onErrorAjaxProviders;

    /**
     * 静态方法，用于将AjaxResult对象写入HTTP响应中
     *
     * @param response   HTTP响应对象
     * @param ajaxResult Ajax结果对象
     */
    public static void writeInfoResp(HttpServletResponse response, AjaxResult ajaxResult) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse)response;
        if (httpResponse.isCommitted()) {
            return;
        }
        httpResponse.setCharacterEncoding("UTF-8");
        httpResponse.setContentType("application/json");
        String jsonResponse = JSON.toJSONString(ajaxResult.getMap());
        try (PrintWriter writer = httpResponse.getWriter()) {
            writer.print(jsonResponse);
            writer.flush();
        }
    }

    /**
     * AjaxUtil实例，用于单例模式
     */
    private static AjaxUtil ajaxUtil;

    /**
     * 获取Ajax工具类实例
     *
     * @return AjaxUtil实例
     */
    public static AjaxUtil getInstance() {
        return ajaxUtil;
    }

    /**
     * 创建Ajax结果对象，用于处理异常
     *
     * @param e 异常对象
     * @return AjaxResult对象
     */
    public final <T extends Throwable> AjaxResult createAjaxResultOnError(T e) {
        if (e == null) {
            return error(AjaxResult.DEFAULT_ERROR_MESSAGE, null);
        }
        Collection<IOnErrorAjaxProvider> providers = onErrorAjaxProviders;
        if (CollUtil.isNotEmpty(providers)) {
            for (IOnErrorAjaxProvider provider : providers) {
                if (provider.support(e.getClass())) {
                    return provider.onError(e);
                }
            }
        }
        return error(e.getLocalizedMessage(), e);
    }

    /**
     * 自定义Ajax响应结果
     *
     * @param code 响应代码
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult custom(int code, String msg, Object data) {
        return builder().code(code).message(msg).data(data).build();
    }

    /**
     * 创建自定义的Ajax结果构建器
     *
     * @return IAjaxResultBuilder对象
     */
    public IAjaxResultBuilder.IAjaxResult1Step createCustom() {
        return ajaxBuilderProvider.getAjaxResultBuilder().create();
    }

    /**
     * 创建流式构建器实例
     * <p>
     * 提供更直观的流式API来构建AjaxResult对象：
     * <pre>
     * AjaxResult result = AjaxUtil.builder()
     *     .code(200)
     *     .message("操作成功")
     *     .data(entity)
     *     .redirect("/admin/user")
     *     .build();
     * </pre>
     * </p>
     *
     * @return AjaxResultFluentBuilder实例
     */
    public static AjaxResultFluentBuilder builder() {
        return AjaxResultFluentBuilder.builder();
    }

    /**
     * 构建成功的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult success(String msg, Object data) {
        return builder().success(msg, data).build();
    }

    /**
     * 构建成功的Ajax响应结果，仅包含数据
     *
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult success(Object data) {
        return getInstance().success(AjaxResult.DEFAULT_SUCCESS_MESSAGE, data);
    }

    /**
     * 构建带重定向的成功Ajax响应结果
     *
     * @param redirect 重定向路径
     * @return AjaxResult对象
     */
    public AjaxResult successWithRedirect(String redirect) {
        return successWithRedirect(null, redirect);
    }

    /**
     * 构建带重定向的成功Ajax响应结果，包含数据
     *
     * @param data     响应数据
     * @param redirect 重定向路径
     * @return AjaxResult对象
     */
    public AjaxResult successWithRedirect(Object data, String redirect) {
        return successWithRedirect(AjaxResult.DEFAULT_SUCCESS_MESSAGE, data, redirect);
    }

    /**
     * 构建带重定向的成功Ajax响应结果，包含消息和数据
     *
     * @param msg      响应消息
     * @param data     响应数据
     * @param redirect 重定向路径
     * @return AjaxResult对象
     */
    public AjaxResult successWithRedirect(String msg, Object data, String redirect) {
        return builder().success(msg, data).redirect(redirect).build();
    }

    /**
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(String msg, Object data, WebMessageTypeEnum webMessageTypeEnum, Integer duration) {
        return builder().error(msg, data).type(webMessageTypeEnum).duration(duration).build();
    }

    /**
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(String msg, Object data) {
        return builder().error(msg, data).build();
    }

    /**
     * 构建调试错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult debugError(String msg, Object data) {
        return builder().debugError(msg, data).build();
    }

    /**
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult debugError(String msg) {
        return builder().debugError(msg).build();
    }

    /**
     * 构建通知错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult noticeError(String msg, Object data, WebMessageTypeEnum webNoticeType, Integer duration,
        String title) {
        return builder().notice(title, msg, webNoticeType).duration(duration).data(data).build();
    }

    /**
     * 构建通知错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult noticeError(String title, String msg, WebMessageTypeEnum webNoticeType, Integer duration) {
        return noticeError(msg, null, webNoticeType, duration, title);
    }

    /**
     * 构建通知错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult noticeError(String title, String msg, WebMessageTypeEnum webNoticeType) {
        return noticeError(title, msg, webNoticeType, 5 * 1000);
    }




    /**
     * 构建错误的Ajax响应结果，仅包含数据
     *
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(Object data) {
        return builder().error(AjaxResult.DEFAULT_ERROR_MESSAGE, data).build();
    }

    @Override
    public void startUp() throws RuntimeException {
        ajaxUtil = this;
        onErrorAjaxProviders =
            SpringUtil.getBeansByTypeSafe(IOnErrorAjaxProvider.class).stream().sorted(Comparators::asc)
                .collect(Collectors.toList());
    }
}
