package com.tbs.web.genric;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.genric.form.GenricParameterForm;
import com.tbs.web.genric.vo.ApiInvokeResultVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于Dubbo泛化调用的泛化实例
 * <p>
 * 使用Dubbo的GenericService进行泛化调用，支持直连指定的服务实例
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class DubboGenricApiInstance extends AbstractGenricApiInstance<Serializable> {

    /**
     * 应用配置
     */
    private final ApplicationConfig applicationConfig;

    /**
     * 注册中心配置
     */
    private final RegistryConfig registryConfig;

    /**
     * 接口全限定名
     */
    private final String interfaceName;

    /**
     * 方法名
     */
    private final String methodName;

    public DubboGenricApiInstance(ApplicationConfig applicationConfig, RegistryConfig registryConfig, String apiId,
        Integer apiVersion, boolean needAuth, String interfaceName, String methodName, String... preAuthBeanNames) {
        super(apiId, apiVersion, needAuth);
        this.applicationConfig = applicationConfig;
        this.registryConfig = registryConfig;
        this.interfaceName = interfaceName;
        this.methodName = methodName;

        // 获取所有可用的预认证实现
        Collection<IApiInstancePreAuth> allPreAuths = SpringUtil.getBeansOfType(IApiInstancePreAuth.class).values();
        Map<String, IApiInstancePreAuth> preAuthMap =
            allPreAuths.stream().collect(Collectors.toMap(IApiInstancePreAuth::getPreAuthId, preAuth -> preAuth));

        // 从预认证ID列表中获取预认证实例
        if (CollUtil.isNotEmpty(preAuthBeanNames)) {
            var preAuthIdsSet = new HashSet<>(Arrays.asList(preAuthBeanNames));
            for (var preAuthId : preAuthIdsSet) {
                try {
                    var preAuth = preAuthMap.get(preAuthId);
                    if (preAuth != null) {
                        this.getPreAuths().add(preAuth);
                    } else {
                        log.warn("预认证ID不存在: {}", preAuthId);
                    }
                } catch (Exception e) {
                    log.warn("获取预验证失败: {}", preAuthId, e);
                }
            }
        }
    }

    @Override
    protected void invoke(Map<String, GenricParameterForm> parameterForms, ApiInvokeResultVo<Serializable> resultVo,
        Optional<UserDetails> userDetails) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        try {
            // 组装 Dubbo 泛化引用：直连指定接口，开启 generic，超时 10s
            reference.setApplication(applicationConfig);
            reference.setRegistry(registryConfig);
            reference.setInterface(interfaceName);
            reference.setGeneric("true");
            reference.setTimeout(10000);

            GenericService genericService = reference.get();

            // 准备参数
            if (parameterForms == null || parameterForms.isEmpty()) {
                // 没有参数的情况
                Object result = genericService.$invoke(methodName, new String[] {}, new Object[] {});
                resultVo.setExecuteResult((Serializable)result);
            } else {
                // 将参数按顺序整理
                List<GenricParameterForm> sortedParams =
                    parameterForms.values().stream().sorted(Comparator.comparingInt(GenricParameterForm::getSort))
                        .collect(Collectors.toList());
                Object[] args = sortedParams.stream().map(GenricParameterForm::getParameterValue).toArray();

                // 获取参数类型
                String[] paramTypes = new String[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (StrUtil.isNotEmpty(sortedParams.get(i).getExceptdClassName())) {
                        paramTypes[i] = sortedParams.get(i).getExceptdClassName();
                    } else if (args[i] != null) {
                        paramTypes[i] = args[i].getClass().getName();
                    } else {
                        paramTypes[i] = "java.lang.Object"; // 使用Object作为默认类型
                    }
                }

                // 调用泛化方法
                Object result = genericService.$invoke(methodName, paramTypes, args);
                resultVo.setExecuteResult((Serializable)result);
            }
        } catch (FormattedRuntimeException e) {
            AssertUtil.isTrue(true).cause(e).throwIt();
        } catch (RuntimeException e) {
            // 不回显底层异常原文（含目标地址、方法签名等），真实原因交给 cause 进日志
            AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .i18n(WebI18nConstant.GENERIC_API_DUBBO_INVOKE_FAILED).cause(e).throwIt();
        } catch (Exception e) {
            // 同上
            AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .i18n(WebI18nConstant.GENERIC_API_DUBBO_INVOKE_FAILED).cause(e).throwIt();
        } finally {
            // 销毁引用，释放资源
            if (reference != null) {
                reference.destroy();
            }
        }
    }
}
