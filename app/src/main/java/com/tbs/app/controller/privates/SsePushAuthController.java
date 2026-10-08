package com.tbs.app.controller.privates;

import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.impl.auth.PushCredentialService;
import com.tbs.push.model.auth.PushAuthForm;
import com.tbs.push.model.auth.PushCredential;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * SSE传输的连接认证薄壳控制器
 * <p>
 * 统一连接建立流程第一步的HTTP接入点：全部逻辑委托协议层
 * {@link PushCredentialService}，本类仅承担MVC注解与响应包装。
 * 与WebSocket侧的同构薄壳控制器由各自引入的启用注解互斥，永不同时激活；
 * 端点路径取自 {@link PushFrameConstants#AUTH_PATH}，改路径只动协议层常量。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@Tag(name = "推送连接认证控制器", description = "推送统一连接建立流程第一步：业务认证并签发一次性建连凭证")
public class SsePushAuthController extends BaseController {

    /**
     * 连接凭证服务
     */
    private final PushCredentialService credentialService;

    /**
     * 构造连接认证薄壳控制器
     *
     * @param credentialService 连接凭证服务
     */
    public SsePushAuthController(PushCredentialService credentialService) {
        this.credentialService = credentialService;
    }

    /**
     * 连接认证并签发一次性凭证
     * <p>
     * 认证表单经协议层凭证服务处理：token（{@link com.tbs.push.model.auth.PushAuthToken}值对象，
     * 必填非空，内容由业务认证器解释）交由IPushAuthenticator SPI校验，通过后预分配sessionId、
     * 签发一次性ticket（TTL默认60秒）入存储。
     * </p>
     * <p>
     * 请求示例：{"token": "<登录令牌>", "userId": "10001", "attributes": {"dept": "it"}}；
     * 认证失败返回统一错误响应（i18n：push.auth.failed），凭证签发被拒绝。
     * </p>
     *
     * @param form 客户端提交的认证表单（token必填非空；userId可选；attributes可选，认证通过后注入会话属性）
     * @return 统一响应（data：PushCredential凭证对象，含ticket一次性凭证、sessionId预分配会话标识、
     *         expireAt过期时间、attributes会话属性）
     */
    @PostMapping(PushFrameConstants.AUTH_PATH)
    @Operation(summary = "连接认证并签发一次性凭证", description = "校验认证表单后签发一次性push-ticket与预分配sessionId，凭证用于SSE建连（TTL默认60秒）")
    @ApiResponse(responseCode = "200", description = "认证成功，data为PushCredential凭证对象；认证失败返回统一错误响应")
    @AjaxResponseType(PushCredential.class)
    public AjaxResult auth(@RequestBody PushAuthForm form) {
        PushCredential credential = credentialService.issue(form);
        return successResult(credential);
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "推送连接认证";
    }

}
