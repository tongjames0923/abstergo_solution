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
 * <p>
 * 抽象一个「通过 id/version 标识、可选需认证、带预认证链」的远程调用单元。
 * 具体调用协议（Dubbo 泛化 / Triple HTTP）由子类实现 {@link #invoke}。
 * {@link #invokeApi} 统一编排：登录校验 → 预认证链 → 参数排序 → 执行（业务/系统异常均捕获并写入结果 VO，不向上抛）。
 * </p>
 *
 * @param <R> 调用返回结果的序列化类型
 * @author abstergo
 */
@Getter
@Slf4j
public abstract class AbstractGenricApiInstance<R extends Serializable> {
    /**
     * 接口唯一标识
     *
     * @return API id
     */
    private String apiId;
    /**
     * 接口版本号
     *
     * @return 版本；为 {@code null} 表示不区分版本
     */
    private Integer apiVersion;
    /**
     * 是否需要认证
     *
     * @return 需要认证返回 {@code true}
     */
    private boolean needAuth;

    /**
     * 预认证处理器列表
     *
     * @return 预认证实例集合
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
     * 执行具体的远程调用
     *
     * @param parameterForms 已按名索引的参数，不能为空
     * @param resultVo       调用结果载体，由实现填充分执行结果/异常
     * @param userDetails   当前用户（需要认证时不为空）
     */
    protected abstract void invoke(Map<String, GenricParameterForm> parameterForms, ApiInvokeResultVo<R> resultVo,
        Optional<UserDetails> userDetails);

    /**
     * 统一调用入口
     * <p>
     * 编排鉴权与预认证，整理参数后执行，并将执行耗时、成功标志、异常信息写入 {@link ApiInvokeResultVo}。
     * 捕获所有异常（含 {@link FormattedRuntimeException}），以结果 VO 形式返回，绝不向上抛出。
     * </p>
     *
     * @param parameterForms 调用参数列表，可以为空
     * @param u 当前用户详情，需要认证时由控制器传入
     * @return 调用结果视图
     */
    public ApiInvokeResultVo<R> invokeApi(List<GenricParameterForm> parameterForms, UserDetails u) {
        var paramMap = new java.util.HashMap<String, GenricParameterForm>();
        ApiInvokeResultVo<R> resultVo = new ApiInvokeResultVo<>();
        AssertUtil.isTrue(isNeedAuth() && u == null).i18n("GENERIC.API.NOT_LOGIN")
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        // 执行预认证链
        if (CollUtil.isNotEmpty(getPreAuths())) {
            for (IApiInstancePreAuth preAuth : getPreAuths()) {
                preAuth.preAuth(u, null, this);
            }
        }

        if (CollUtil.isEmpty(parameterForms)) {
            invoke(paramMap, resultVo, Optional.ofNullable(u));
            return resultVo;
        }
        // 按请求顺序排序参数，保证与远程方法签名顺序一致
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
