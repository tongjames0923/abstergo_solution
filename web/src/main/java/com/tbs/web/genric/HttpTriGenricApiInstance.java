package com.tbs.web.genric;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.genric.form.GenricParameterForm;
import com.tbs.web.genric.vo.ApiInvokeResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 基于HTTP调用Dubbo Triple协议的泛化实例
 * <p>
 * 利用Dubbo 3.3+ Triple协议的HTTP支持，直接通过HTTP POST调用远程服务。 调用格式: POST http://{host}:{port}/{interfaceName}/{methodName}
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public class HttpTriGenricApiInstance extends AbstractGenricApiInstance<Serializable> {

    private static final HttpClient HTTP_CLIENT =
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).executor(Executors.newFixedThreadPool(4))
            .build();

    /**
     * 服务实例地址 (host:port)
     */
    private final String baseUrl;

    /**
     * 接口全限定名
     */
    private final String interfaceName;

    /**
     * 方法名
     */
    private final String methodName;


    public HttpTriGenricApiInstance(String apiId, Integer apiVersion, boolean needAuth, String baseUrl,
        String interfaceName, String methodName, String... preAuthBeanNames) {
        super(apiId, apiVersion, needAuth);
        this.baseUrl = baseUrl;
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
        try {
            String url = String.format("http://%s/%s/%s", baseUrl, interfaceName, methodName);
            // 根据Dubbo Triple协议，将参数转换为数组格式
            Object[] paramArray =
                parameterForms.values().stream().map(GenricParameterForm::getParameterValue).toArray();
            String jsonBody = toJsonArray(paramArray);
            HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(url)).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String value = response.body();

                try {
                    // 首先尝试直接解析为数组
                    var r = JSON.parseArray(value, HashMap.class);
                    if (CollUtil.isEmpty(r)) {
                        // 如果解析为空，尝试加上括号解析（原逻辑）
                        String jsonArray = "[" + value + "]";
                        var r2 = JSON.parseArray(jsonArray, HashMap.class);
                        if (CollUtil.isEmpty(r2)) {
                            resultVo.setExecuteResult(value);
                        } else {
                            ArrayList<HashMap> list = new ArrayList<>();
                            list.addAll(r2);
                            resultVo.setExecuteResult((Serializable) list);
                        }
                    } else {
                        // 直接使用解析结果
                        resultVo.setExecuteResult((Serializable) r);
                    }
                } catch (Exception e) {
                    try {
                        // 如果解析为数组失败，尝试解析为对象
                        var obj = JSON.parseObject(value, HashMap.class);
                        if (obj != null) {
                            resultVo.setExecuteResult((Serializable) obj);
                        } else {
                            resultVo.setExecuteResult(value);
                        }
                    } catch (Exception ex) {
                        resultVo.setExecuteResult(value);
                    }
                }

            } else {
                log.warn("Triple HTTP调用失败, url={}, status={}, body={}", url, response.statusCode(),
                    response.body());
                throw new RuntimeException("Triple HTTP调用失败: HTTP " + response.statusCode());
            }
        } catch (FormattedRuntimeException e) {
            AssertUtil.isTrue(true).cause(e).throwIt();
        } catch (RuntimeException e) {
            AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .message("Triple HTTP调用异常: {}", e.getMessage()).throwIt();
        } catch (Exception e) {
            AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
                .message("Triple HTTP调用异常: {}", e.getMessage()).throwIt();
        }
    }

    private String toJsonArray(Object[] array) {
        return "[" + java.util.Arrays.stream(array).map(this::serializeValue).collect(Collectors.joining(",")) + "]";
    }

    private String serializeValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        return "\"" + escape(value.toString()) + "\"";
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
