package com.tbs.web.genric;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.genric.config.GenericApiConfig;
import com.tbs.web.genric.config.GenericApiParamConfig;
import com.tbs.web.genric.form.GenricParameterForm;
import com.tbs.web.genric.vo.ApiInvokeResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.*;

/**
 * 配置驱动的泛化调用目标（新版链路）
 * <p>
 * 一次泛化调用的执行单元：坐标与参数定义全部来自登记（{@link GenericApiConfig}），调用走 Dubbo 泛化引用，
 * 因此网关不需要目标接口的 jar，服务方也不需要实现任何自定义契约。
 * </p>
 * <p>
 * 入口是 {@link #invokeByConfig(JsonNode, UserDetails)}。父类的 {@code invokeApi(...)} 编排<b>不可用于本类</b>：
 * 它按参数名把实参折成 {@code Map}，重名或未登记名字的参数会互相覆盖丢参，属于旧链路既有行为；
 * 本类改用有序装配（{@link GenericArgumentAssembler}），故父类 {@code invoke} 实现为显式不支持。
 * 继承父类只为复用标识字段、预认证容器与既有 {@link IApiInstancePreAuth} 签名，不改旧链路一行代码。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class ConfiguredGenricApiInstance extends AbstractGenricApiInstance<Serializable> {

    /**
     * 泛化调用配置（接口/方法/坐标/参数定义）
     */
    private final GenericApiConfig config;

    /**
     * 泛化引用持有器
     */
    private final GenericReferenceHolder referenceHolder;

    public ConfiguredGenricApiInstance(GenericApiConfig config, GenericReferenceHolder referenceHolder,
        Map<String, IApiInstancePreAuth> preAuthMap) {
        super(config.getApiId(), config.getVersion(), config.isNeedAuth());
        this.config = config;
        this.referenceHolder = referenceHolder;
        setPreAuths(resolvePreAuths(config.getPreAuthBeans(), preAuthMap));
    }

    /**
     * 旧编排入口，本类不支持
     * <p>
     * 仅作显式拦截：一旦被当作入口调用，说明新链路误用了旧的 {@code invokeApi} 语义，宁可报错也不静默丢参。
     * </p>
     *
     * @param parameterForms 旧格式参数
     * @param resultVo       结果载体
     * @param userDetails    当前用户
     */
    @Override
    protected void invoke(Map<String, GenricParameterForm> parameterForms, ApiInvokeResultVo<Serializable> resultVo,
        Optional<UserDetails> userDetails) {
        throw AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
            .i18n(WebI18nConstant.GENERIC_API_ENTRY_UNSUPPORTED).params(getApiId()).build();
    }

    /**
     * 按登记定义执行一次泛化调用
     * <p>
     * 编排顺序：登录校验 → 预认证链 → 实参装配 → 取泛化引用 → 执行。前三步任一失败都直接抛给全局异常处理，
     * 因为只有远端执行失败才写进结果视图的双层成功标志（与旧链路语义保持一致）。
     * </p>
     *
     * @param body        请求体 JSON 节点，命名式对象或位置式数组，可以为 {@code null}
     * @param userDetails 当前用户详情，需要认证时不为空
     * @return 调用结果视图
     */
    public ApiInvokeResultVo<Serializable> invokeByConfig(JsonNode body, UserDetails userDetails) {
        ApiInvokeResultVo<Serializable> resultVo = new ApiInvokeResultVo<>();
        AssertUtil.isTrue(isNeedAuth() && userDetails == null).i18n(WebI18nConstant.GENERIC_API_NOT_LOGIN)
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).throwIt();
        executePreAuths(userDetails);
        GenericArgumentAssembler.AssembledArguments assembled = GenericArgumentAssembler.assemble(config, body);
        GenericService genericService =
            referenceHolder.genericService(config.getInterfaceName(), config.getBusiness(), config.getVersion());
        Date begin = new Date();
        try {
            Object result =
                genericService.$invoke(config.getMethodName(), assembled.getParameterTypes(),
                    assembled.getArguments());
            AssertUtil.isTrue(result != null && !(result instanceof Serializable))
                .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).i18n(WebI18nConstant.GENERIC_API_RESULT_NOT_SERIALIZABLE)
                .params(result.getClass().getName()).throwIt();
            resultVo.setExecuteResult((Serializable)result);
            resultVo.setSuccessFlag(1);
        } catch (FormattedRuntimeException e) {
            resultVo.setSuccessFlag(0);
            resultVo.setExceptionClassName(e.getClass().getName());
            resultVo.setExceptionMessage(e.getMessage());
            resultVo.setExceptionObject(e);
        } catch (Exception e) {
            resultVo.setSuccessFlag(0);
            // 不回显底层异常原文（含目标地址、方法签名等），真实原因交给日志
            resultVo.setExceptionClassName(e.getClass().getName());
            resultVo.setExceptionMessage(I18nUtil.get(WebI18nConstant.GENERIC_API_DUBBO_INVOKE_FAILED));
            resultVo.setExceptionObject(stackTraceOf(e));
            log.warn("泛化调用执行失败, apiId={}, 接口={}, 方法={}", getApiId(), config.getInterfaceName(),
                config.getMethodName(), e);
        }
        Date end = new Date();
        resultVo.setBeginTime(begin);
        resultVo.setEndTime(end);
        return resultVo;
    }

    /**
     * 对外透出的入参定义（供元数据接口渲染表单）
     *
     * @return 按签名顺序排列的参数定义，无登记时为空列表
     */
    public List<GenericApiParamConfig> paramsMeta() {
        return config.getParams();
    }

    /**
     * 依次触发预认证链
     *
     * @param userDetails 当前用户详情，可以为 {@code null}
     */
    private void executePreAuths(UserDetails userDetails) {
        if (getPreAuths() == null) {
            return;
        }
        for (IApiInstancePreAuth preAuth : getPreAuths()) {
            preAuth.preAuth(userDetails, null, this);
        }
    }

    /**
     * 把登记的预认证标识解析为处理器实例
     * <p>
     * 未匹配到的标识只记录告警并跳过，与旧链路一致：配置的鉴权缺失不应让调用静默放行，但也不该在装配阶段炸掉整个请求，
     * 真正的权限判定由各处理器自己完成。
     * </p>
     *
     * @param preAuthBeans 逗号分隔的标识串，可以为空
     * @param preAuthMap   容器内处理器索引
     * @return 命中的处理器列表
     */
    private static List<IApiInstancePreAuth> resolvePreAuths(String preAuthBeans,
        Map<String, IApiInstancePreAuth> preAuthMap) {
        List<IApiInstancePreAuth> preAuths = new ArrayList<>(4);
        if (StrUtil.isBlank(preAuthBeans)) {
            return preAuths;
        }
        for (String preAuthId : new LinkedHashSet<>(Arrays.asList(preAuthBeans.split(",")))) {
            String key = preAuthId.trim();
            if (key.isEmpty()) {
                continue;
            }
            IApiInstancePreAuth preAuth = preAuthMap.get(key);
            if (preAuth == null) {
                log.warn("预认证ID不存在: {}", key);
                continue;
            }
            preAuths.add(preAuth);
        }
        return preAuths;
    }

    /**
     * 取异常堆栈文本
     *
     * @param e 异常，不能为空
     * @return 堆栈文本
     */
    private static String stackTraceOf(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            e.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }
}
