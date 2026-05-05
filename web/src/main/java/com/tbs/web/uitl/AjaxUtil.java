package com.tbs.web.uitl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.IStartup;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.common.util.function.Comparators;
import com.tbs.web.enums.WebMessageTypeEnum;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IAjaxResultBuilder;
import com.tbs.web.result.IOnErrorAjaxProvider;
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

    private Collection<IOnErrorAjaxProvider> onErrorAjaxProviders;

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
        PrintWriter writer = null;
        writer = httpResponse.getWriter();
        writer.print(jsonResponse);
        writer.flush();
        writer.close();
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
        if (CollUtil.isNotEmpty(getInstance().onErrorAjaxProviders)) {
            for (IOnErrorAjaxProvider provider : getInstance().onErrorAjaxProviders) {
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
        return createCustom().appendField(AjaxResult.MESSAGE_KEY, msg).appendField(AjaxResult.CODE_KEY, code)
            .appendField(AjaxResult.DATA_KEY, data).next().build();
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
     * 构建成功的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult success(String msg, Object data) {
        return getInstance().custom(AjaxResult.DEFAULT_SUCCESS_CODE, msg, data);
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
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(String msg, Object data, WebMessageTypeEnum webMessageTypeEnum, Integer duration) {
        var map = getInstance().custom(AjaxResult.DEFAULT_ERROR_CODE, msg, data).getMap();
        if (webMessageTypeEnum != null) {
            map.put("type", webMessageTypeEnum.getValue());
        }
        if (duration != null && duration > 0) {
            map.put("duration", duration);
        }
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setMap(map);
        return ajaxResult;
    }

    /**
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(String msg, Object data) {
        return getInstance().error(msg, data, WebMessageTypeEnum.ERROR, 10 * 1000);
    }

    /**
     * 构建调试错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult debugError(String msg, Object data) {
        return getInstance().custom(AjaxResult.DEBUG_ERROR_CODE, msg, data);
    }

    /**
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult debugError(String msg) {
        return getInstance().custom(AjaxResult.DEBUG_ERROR_CODE, msg, null);
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
        var map = getInstance().custom(AjaxResult.NOTICE_CODE, msg, data).getMap();
        if (webNoticeType != null) {
            map.put("type", webNoticeType.getValue());
        }
        if (duration != null && duration > 0) {
            map.put("duration", duration);
        }
        if (StrUtil.isNotEmpty(title)) {
            map.put("title", title);
        }
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.setMap(map);
        return ajaxResult;
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
        return noticeError(msg, msg, webNoticeType, 5 * 1000);
    }




    /**
     * 构建错误的Ajax响应结果，仅包含数据
     *
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(Object data) {
        return getInstance().error(AjaxResult.DEFAULT_ERROR_MESSAGE, data, WebMessageTypeEnum.ERROR, 10 * 1000);
    }

    @Override
    public void startUp() throws RuntimeException {
        ajaxUtil = this;
        this.onErrorAjaxProviders =
            SpringUtil.getBeansByTypeSafe(IOnErrorAjaxProvider.class).stream().sorted(Comparators::asc)
                .collect(Collectors.toList());
    }
}
