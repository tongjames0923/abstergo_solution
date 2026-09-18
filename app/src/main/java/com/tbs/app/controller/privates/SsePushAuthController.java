package com.tbs.app.controller.privates;

import com.tbs.push.impl.PushCredentialService;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.model.PushAuthForm;
import com.tbs.push.model.PushCredential;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

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
     *
     * @param form 客户端提交的认证表单
     * @return 统一响应（data：sessionId/ticket/expireAt/version）
     */
    @PostMapping(PushFrameConstants.AUTH_PATH)
    public AjaxResult auth(@RequestBody PushAuthForm form) {
        PushCredential credential = credentialService.issue(form);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sessionId", credential.getSessionId());
        data.put("ticket", credential.getTicket());
        data.put("expireAt", credential.getExpireAt());
        data.put("version", PushFrameConstants.PROTOCOL_VERSION);
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
