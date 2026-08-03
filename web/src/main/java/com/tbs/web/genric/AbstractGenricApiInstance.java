package com.tbs.web.genric;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.genric.form.GenricParameterForm;
import com.tbs.web.genric.vo.ApiInvokeResultVo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;

/**
 * 泛化接口实例
 *
 * @author abstergo
 */

@Getter
@Slf4j
public abstract class AbstractGenricApiInstance<R extends Serializable> {
    /**
     * -- GETTER -- 获取接口唯一标识
     *
     * @return
     */
    private String apiId;
    /**
     * -- GETTER -- 获取接口版本号
     *
     * @return
     */
    private Integer apiVersion;
    /**
     * -- GETTER -- 是否需要认证
     *
     * @return
     */
    private boolean needAuth;

    /**
     * -- GETTER -- 获取预认证列表
     *
     *
     * -- SETTER -- 设置预认证列表
     *
     * @return
     * @param preAuths
     */
    @Setter
    private List<IApiInstancePreAuth> preAuths;

    public AbstractGenricApiInstance(String apiId, Integer apiVersion, boolean needAuth, IApiInstancePreAuth... auths) {
        this.apiId = apiId;
        this.apiVersion = apiVersion;
        this.needAuth = needAuth;
        if (CollUtil.isNotEmpty(auths)) {
            this.preAuths = Arrays.asList(auths);
        } else {
            this.preAuths = new ArrayList<>(4);
        }
    }

    /**
     * 调用接口
     *
     * @param parameterForms 参数
     * @param resultVo       调用结果
     * @param userDetails   当前用户
     */
    protected abstract void invoke(Map<String, GenricParameterForm> parameterForms, ApiInvokeResultVo<R> resultVo,
        Optional<UserDetails> userDetails);

    /**
     * 调用接口
     *
     * @param parameterForms 参数
     * @param userDetails    当前用户
     * @return
     */
    public ApiInvokeResultVo<R> invokeApi(List<GenricParameterForm> parameterForms, UserDetails u) {
        var paramMap = new java.util.HashMap<String, GenricParameterForm>();
        ApiInvokeResultVo<R> resultVo = new ApiInvokeResultVo<>();
        AssertUtil.isTrue(isNeedAuth() && u == null).i18n("GENERIC.API.NOT_LOGIN")
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        // 执行预认证
        if (CollUtil.isNotEmpty(getPreAuths())) {
            for (IApiInstancePreAuth preAuth : getPreAuths()) {
                preAuth.preAuth(u, null, this);
            }
        }

        if (CollUtil.isEmpty(parameterForms)) {
            invoke(paramMap, resultVo, Optional.ofNullable(u));
            return resultVo;
        }
        int i = 0;
        for (GenricParameterForm parameterForm : parameterForms) {
            parameterForm.setSort(i++);
            paramMap.put(parameterForm.getParameterName(), parameterForm);
        }
        Date beg = new Date();
        try {
            invoke(paramMap, resultVo, Optional.ofNullable(u));
            resultVo.setSuccessFlag(1);
        } catch (FormattedRuntimeException e) {
            resultVo.setSuccessFlag(0);
            resultVo.setExceptionClassName(e.getClass().getName());
            resultVo.setExceptionMessage(e.getMessage());
            resultVo.setExceptionObject(e);
        } catch (Exception e) {
            resultVo.setSuccessFlag(0);
            resultVo.setExceptionClassName(e.getClass().getName());
            resultVo.setExceptionMessage(e.getMessage());
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionStackTrace = sw.toString();
            try {
                pw.close();
                sw.close();
            } catch (Exception closeException) {
                log.error("Error closing StringWriter and PrintWriter", closeException);
            }
            resultVo.setExceptionObject(exceptionStackTrace);
        }
        Date end = new Date();
        resultVo.setBeginTime(beg);
        resultVo.setEndTime(end);
        return resultVo;

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbstractGenricApiInstance)) {
            return false;
        }
        AbstractGenricApiInstance<?> that = (AbstractGenricApiInstance<?>)o;
        return Objects.equals(getApiId(), that.getApiId()) && Objects.equals(getApiVersion(), that.getApiVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApiId(), getApiVersion());
    }
}
