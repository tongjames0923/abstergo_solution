package com.tbs.app.service.impl.push;

import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.api.session.IUploadGuard;
import com.tbs.push.constants.PushSessionAttrConstants;
import com.tbs.push.model.frame.PushFrame;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 会话归属上行守卫（上行时点认人，跨传输统一生效）
 * <p>
 * 挂载在协议层上行守卫链上，SSE HTTP upload 与 WebSocket 帧上行均被覆盖：
 * 会话认证阶段绑定的用户名与当前请求登录主体比对，不一致拒绝该帧上行
 * （协议层回发ERROR帧丢弃，不关闭连接）。会话未绑定身份（无鉴权部署）
 * 或取不到认证上下文（非HTTP线程，如WebSocket IO线程——建连时点已由
 * {@link PushSessionIdentityInterceptor}做过一致性校验）时放行。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class PushSessionUploadGuard implements IUploadGuard {

    /**
     * 判定会话可否上行指定协议帧
     *
     * @param session 来源会话（携带认证阶段绑定的身份属性）
     * @param frame   待处理的协议帧
     * @return true表示归属一致或无法判定放行，false表示拒绝该帧
     */
    @Override
    public boolean permit(IConnectionSession session, PushFrame frame) {
        String boundUsername = session.getAttribute(PushSessionAttrConstants.USERNAME);
        if (StrUtil.isEmpty(boundUsername)) {
            return true;
        }
        String currentUsername;
        try {
            currentUsername = AuthUtil.getInstance().getCurrentUser().getUsername();
        } catch (Exception error) {
            return true;
        }
        if (StrUtil.isEmpty(currentUsername) || boundUsername.equals(currentUsername)) {
            return true;
        }
        log.warn("上行主体与会话绑定身份不一致, sessionId:{}, bound:{}, current:{}", session.getSessionId(),
            boundUsername, currentUsername);
        return false;
    }

}
