package com.tbs.web.uitl;

import com.tbs.common.IStartup;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.Comparators;
import com.tbs.web.enums.WebMessageTypeEnum;
import com.tbs.web.result.*;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Ajax 工具类
 * <p>
 * 构建与写出统一的 {@link AjaxResult} 响应：封装成功/失败/错误/通知/重定向等多形态结果，
 * 以及异常到 {@link AjaxResult} 的统一转换（按 {@link IOnErrorAjaxProvider} 顺序匹配）。
 * 同时负责将结果以 JSON 写入 {@link HttpServletResponse}。通过 {@code IStartup} 在启动时收集错误处理提供者。
 * </p>
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
        String jsonResponse = JsonUtils.toJSONString(ajaxResult.getMap());
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
     * <p>
     * 该实例在 {@link #startUp()} 时被赋值；但启动窗口（如异步启动、ApplicationRunner 尚未执行）
     * 内可能被 Security 认证入口等提前调用，故在未初始化时返回一个临时实例兜底。
     * {@code custom/error/success/debugError/noticeError} 等方法仅依赖静态 builder，不依赖注入字段，
     * 兜底实例可正常工作；待 startUp 完成后会被真正的 Spring Bean 覆盖。
     * </p>
     *
     * @return AjaxUtil实例
     */
    public static AjaxUtil getInstance() {
        AjaxUtil instance = ajaxUtil;
        return instance != null ? instance : new AjaxUtil();
    }

    /**
     * 创建Ajax结果对象，用于处理异常
     *
     * @param e 异常对象
     * @return AjaxResult对象
     */
    public final <T extends Throwable> AjaxResult createAjaxResultOnError(T e) {
        if (e == null) {
            return error(AjaxResult.getDefaultErrorMessage(), null);
        }
        Collection<IOnErrorAjaxProvider> providers = onErrorAjaxProviders;
        if (CollUtil.isNotEmpty(providers)) {
            for (IOnErrorAjaxProvider provider : providers) {
                if (provider.support(e.getClass())) {
                    return provider.onError(e);
                }
            }
        }
        // 无提供者命中时的兜底：自建异常回传自身文案，其余异常只给通用提示，
        // 且绝不把异常对象塞进响应 data（避免堆栈被序列化外泄）
        return error(resolveErrorMessage(e), null);
    }

    /**
     * 解析面向客户端的错误消息
     * <p>
     * 约定：自建异常（实现 {@link IExceptionLevel}，即 AssertUtil 构建的异常及其子类）的消息就是面向调用方的
     * 文案，直接回传；框架/第三方抛出的原始异常一律回退通用提示——其消息常含 SQL 语句、连接串、内部类名
     * 等实现细节，经响应体透出即信息泄露。真实原因不会丢失，全局异常处理以错误日志记录了含 cause 链的完整
     * 堆栈，排查以日志与 traceId 为准。
     * </p>
     * <p>
     * 由此推导出一条约束：自建异常的消息不得拼接外部异常原文（如 {@code e.getMessage()}），否则本条约定
     * 会被绕过。
     * </p>
     *
     * @param e 待转换异常，允许为null
     * @return 响应消息，不会为null
     */
    public static String resolveErrorMessage(Throwable e) {
        if (e instanceof IExceptionLevel) {
            return StrUtil.emptyToDefault(e.getLocalizedMessage(), AjaxResult.getDefaultErrorMessage());
        }
        return AjaxResult.getDefaultErrorMessage();
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
        return getInstance().success(AjaxResult.getDefaultSuccessMessage(), data);
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
        return successWithRedirect(AjaxResult.getDefaultSuccessMessage(), data, redirect);
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
     * 构建调试错误的Ajax响应结果，仅包含消息
     *
     * @param msg 响应消息
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
     * 构建通知错误的Ajax响应结果，包含标题、消息、通知类型与持续时间
     *
     * @param title          通知标题
     * @param msg            响应消息
     * @param webNoticeType  通知类型
     * @param duration       持续时间（毫秒）
     * @return AjaxResult对象
     */
    public AjaxResult noticeError(String title, String msg, WebMessageTypeEnum webNoticeType, Integer duration) {
        return noticeError(msg, null, webNoticeType, duration, title);
    }

    /**
     * 构建通知错误的Ajax响应结果，包含标题、消息与通知类型
     *
     * @param title         通知标题
     * @param msg           响应消息
     * @param webNoticeType 通知类型
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
        return builder().error(AjaxResult.getDefaultErrorMessage(), data).build();
    }

    @Override
    public void startUp() throws RuntimeException {
        ajaxUtil = this;
        onErrorAjaxProviders =
            SpringUtil.getBeansByTypeSafe(IOnErrorAjaxProvider.class).stream().sorted(Comparators::asc)
                .collect(Collectors.toList());
    }
}
