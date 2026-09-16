package com.tbs.app.security.grant.wecom;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.service.impl.CacheNamesConstants;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 企业微信 API 客户端
 * <p>
 * 封装企业微信服务端接口调用：获取应用 access_token（带缓存）与按授权码换取用户身份。
 * access_token 有效期 7200 秒，经 {@code @Cacheable} + {@code @ExpireOn} 缓存 110 分钟，
 * 避免高频调用 gettoken；token 获取与换票拆为独立方法，由调用方编排以规避代理自调用失效。
 * </p>
 *
 * @author abstergo
 */
public class WecomApiClient {

    /**
     * 获取应用 access_token 接口地址
     */
    private static final String GET_TOKEN_URL = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";

    /**
     * 按授权码获取用户身份接口地址
     */
    private static final String GET_USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo";

    private final WecomProperty property;

    private final RestTemplate restTemplate;

    /**
     * 构建企业微信 API 客户端
     *
     * @param property 企业微信登录配置属性
     */
    public WecomApiClient(WecomProperty property) {
        this.property = property;
        this.restTemplate = new RestTemplate();
    }

    /**
     * 获取应用 access_token（带缓存）
     * <p>
     * 企业微信 access_token 有效期 7200 秒，缓存 110 分钟留安全余量；
     * 缓存 key 固定为单应用 token，命中期内不再调用 gettoken。
     * </p>
     *
     * @return 应用 access_token
     */
    @Cacheable(cacheNames = CacheNamesConstants.WECOM_ACCESS_TOKEN, key = "'access_token'")
    @ExpireOn(value = 110, timeUnit = TimeUnit.MINUTES)
    public String getAccessToken() {
        AssertUtil.isTrue(StrUtil.isEmpty(property.getCorpId()) || StrUtil.isEmpty(property.getCorpSecret()))
            .i18n(SystemI18nConstant.WECOM_CONFIG_MISSING).errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        Map<String, Object> response = getForMap(UriComponentsBuilder.fromHttpUrl(GET_TOKEN_URL)
            .queryParam("corpid", property.getCorpId())
            .queryParam("corpsecret", property.getCorpSecret()));
        checkWecomResponse(response);
        return String.valueOf(response.get("access_token"));
    }

    /**
     * 按授权码换取企业微信用户标识
     *
     * @param accessToken 应用 access_token
     * @param code        企业微信 OAuth 授权码（一次性）
     * @return 企业微信用户标识（userid）
     */
    public String getUserIdByCode(String accessToken, String code) {
        Map<String, Object> response = getForMap(UriComponentsBuilder.fromHttpUrl(GET_USER_INFO_URL)
            .queryParam("access_token", accessToken)
            .queryParam("code", code));
        checkWecomResponse(response);
        Object userId = response.get("userid");
        AssertUtil.isTrue(userId == null || StrUtil.isEmpty(String.valueOf(userId)))
            .i18n(SystemI18nConstant.WECOM_CODE_INVALID).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return String.valueOf(userId);
    }

    /**
     * 执行 GET 请求并解析为响应映射
     *
     * @param builder 已组装查询参数的 URI 构建器
     * @return 响应字段映射，请求异常时由 RestTemplate 抛出
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getForMap(UriComponentsBuilder builder) {
        return restTemplate.getForObject(builder.toUriString(), Map.class);
    }

    /**
     * 校验企业微信接口响应
     * <p>
     * 企业微信约定 {@code errcode=0} 为成功；非 0 时携带 errmsg 抛业务异常。
     * </p>
     *
     * @param response 接口响应映射
     */
    private void checkWecomResponse(Map<String, Object> response) {
        AssertUtil.isTrue(response == null).i18n(SystemI18nConstant.WECOM_API_ERROR).params("empty response")
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        Object errcode = response.get("errcode");
        AssertUtil.isTrue(errcode != null && ((Number) errcode).intValue() != 0)
            .i18n(SystemI18nConstant.WECOM_API_ERROR).params(response.get("errmsg"))
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
    }
}
