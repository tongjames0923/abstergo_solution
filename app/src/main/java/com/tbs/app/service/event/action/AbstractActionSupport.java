package com.tbs.app.service.event.action;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.script.api.expressible.ITriggerAction;
import com.tbs.script.api.expressible.vo.param.ParamValue;

import java.util.Collection;

/**
 * 触发器动作抽象基类。
 *
 * <p>统一提供参数按位读取与国际化文案解析能力，与取值器基类保持一致的参数对齐语义：
 * 按下标定位而非按内容过滤，缺失参数返回 null 由子类决定校验强度。</p>
 *
 * @author abstergo
 */
abstract class AbstractActionSupport implements ITriggerAction {

    /**
     * 读取指定下标的参数值。
     *
     * @param paramValues 参数值集合
     * @param index       参数下标，从 0 开始
     * @return 参数值字符串；参数不存在时返回 null
     */
    protected String paramString(Collection<ParamValue> paramValues, int index) {
        if (CollUtil.isEmpty(paramValues) || index < 0) {
            return null;
        }
        int current = 0;
        for (ParamValue paramValue : paramValues) {
            if (current == index) {
                return paramValue == null ? null : paramValue.getValue();
            }
            current++;
        }
        return null;
    }

    /**
     * 解析国际化文案。
     *
     * @param key 资源 key
     * @return 文案内容；无可用资源时回退为 key 本身
     */
    protected String text(String key) {
        String value = I18nUtil.get(key);
        return value == null || value.isBlank() ? key : value;
    }
}
