package com.tbs.web.genric;

import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.BaseController;
import com.tbs.web.genric.form.GenricParameterForm;
import com.tbs.web.genric.form.HttpInvokeForm;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collection;
import java.util.List;

/**
 * 抽象泛化接口控制器
 * <p>
 * 提供统一的「泛化 API 调用」入口（{@code POST /invoke}）：根据请求中的 apiId/version 匹配实例，
 * 择先匹配无版本实例、无版本请求、或版本一致的实例执行，并以统一响应返回调用结果。
 * 子类按认证维度派生（见 {@code AbstractAuthGenricApiController}/{@code AbstractNoAuthGenricApiController}）。
 * </p>
 *
 * @author abstergo
 */
public abstract class AbstractGenricApiController extends BaseController {

    /**
     * 获取指定 API 的实例集合
     *
     * @param id 泛化 API 唯一标识，不能为空
     * @return 匹配的实例集合，无匹配时返回空集合
     */
    protected abstract Collection<AbstractGenricApiInstance> getGenricApiInstance(String id);

    /**
     * 判断当前控制器是否支持该实例
     *
     * @param instance 泛化实例，不能为空
     * @return 支持返回 {@code true}
     */
    protected abstract boolean support(AbstractGenricApiInstance instance);

    /**
     * 获取当前登录用户
     *
     * @return 当前用户详情；未登录场景（如匿名控制器）返回 {@code null}
     */
    protected abstract UserDetails getCurrentUser();

    @PostMapping("/invoke")
    public AjaxResult invoke(@RequestBody HttpInvokeForm form) {
        String apiId = form.getId();
        Integer apiVersion = form.getVersion();
        List<GenricParameterForm> parameterForms = form.getParameters();
        var instances = getGenricApiInstance(apiId);
        if (CollUtil.isEmpty(instances)) {
            return AjaxUtil.getInstance().error(I18nUtil.get("GENERIC.API.ID_NOT_FOUND"));
        }
        // 匹配优先级：无版本实例 > 未指定版本请求 > 版本精确一致，命中即执行并返回
        for (var instance : instances) {
            if (instance.getApiVersion() == null) {
                return AjaxUtil.getInstance().success(instance.invokeApi(parameterForms, getCurrentUser()));
            }
            if (apiVersion == null) {
                return AjaxUtil.getInstance().success(instance.invokeApi(parameterForms, getCurrentUser()));
            }
            if (instance.getApiVersion().equals(apiVersion)) {
                return AjaxUtil.getInstance().success(instance.invokeApi(parameterForms, getCurrentUser()));
            }
        }
        return AjaxUtil.getInstance().error(I18nUtil.get("GENERIC.API.API_VERSION_NOT_FOUND"));
    }

}
