package com.tbs.web.uitl;

import com.alibaba.fastjson2.JSON;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxBuilderProvider;
import com.tbs.web.result.IAjaxResultBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Ajax工具类，用于构建统一的Ajax响应结果 提供了成功、失败和错误等不同情况下的响应结果构建方法
 *
 * @author tongj
 */
@Component
public class AjaxUtil {

    /**
     * IAjaxBuilderProvider实例，用于创建AjaxResult对象
     */
    @Resource
    private IAjaxBuilderProvider ajaxBuilderProvider;

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
     * 初始化方法，设置静态实例为当前实例，确保单例模式
     */
    @PostConstruct
    public void init() {
        ajaxUtil = this;
    }

    /**
     * 获取Ajax工具类实例
     *
     * @return AjaxUtil实例
     */
    public static AjaxUtil getInstance() {
        return ajaxUtil;
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
     * 构建失败的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult fail(String msg, Object data) {
        return getInstance().custom(AjaxResult.DEFAULT_FAIL_CODE, msg, data);
    }

    /**
     * 构建失败的Ajax响应结果，仅包含数据
     *
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult fail(Object data) {
        return getInstance().fail(AjaxResult.DEFAULT_FAIL_MESSAGE, data);
    }

    /**
     * 构建错误的Ajax响应结果，包含消息和数据
     *
     * @param msg  响应消息
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(String msg, Object data) {
        return getInstance().custom(AjaxResult.DEFAULT_ERROR_CODE, msg, data);
    }

    /**
     * 构建错误的Ajax响应结果，仅包含数据
     *
     * @param data 响应数据
     * @return AjaxResult对象
     */
    public AjaxResult error(Object data) {
        return getInstance().error(AjaxResult.DEFAULT_ERROR_MESSAGE, data);
    }
}
