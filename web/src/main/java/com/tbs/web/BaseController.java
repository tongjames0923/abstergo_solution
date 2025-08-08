package com.tbs.web;

import com.tbs.web.result.AjaxResult;
import com.tbs.web.trace.core.TraceObject;
import com.tbs.web.uitl.AjaxUtil;
import lombok.Getter;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;

/**
 * 基础的控制器, 提供获取当前用户和TokenModel的方法
 *
 * @author tongj
 */
@Getter
public class BaseController {

    /**
     * -- GETTER -- 获取当前用户的TraceObject
     *
     * @return
     */
    @Lazy
    @Resource
    private TraceObject traceObject;


    /**
     * 默认执行的操作成功返回值
     *
     * @param val
     * @return
     */
    public AjaxResult successResult(Object val) {
        return AjaxUtil.getInstance().success("操作成功", val);
    }

    /**
     * 默认执行的操作失败返回值
     *
     * @param e
     * @return
     */
    public AjaxResult errorResult(Throwable e) {
        return AjaxUtil.getInstance().error("系统异常。" + e.getMessage(), e);
    }

}
