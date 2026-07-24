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
 *
 * @author abstergo
 */
public abstract class AbstractGenricApiController extends BaseController {

    /**
     * 获取实例
     *
     * @param id
     * @param version
     * @return
     */
    protected abstract Collection<AbstractGenricApiInstance> getGenricApiInstance(String id);

    /**
     * 是否支持该实例
     *
     * @param instance
     * @return
     */
    protected abstract boolean support(AbstractGenricApiInstance instance);

    /**
     * 获取当前用户
     *
     * @return
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
