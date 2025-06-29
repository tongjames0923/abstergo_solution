package com.tbs.web.result;

/**
 * 请求结果生成接口
 *
 * @author tongj
 */
public interface IAjaxResultBuilder {

    /**
     * 创建一个结果
     *
     * @param code
     * @param message
     * @return
     */
    IAjaxResultBuilder create();

    /**
     * 给结果中加入一个字段
     *
     * @param feild
     * @param value
     * @return
     */
    IAjaxResultBuilder appendField(String feild, Object value);

    /**
     * 构建结果
     *
     * @return
     */
    AjaxResult build();
}
