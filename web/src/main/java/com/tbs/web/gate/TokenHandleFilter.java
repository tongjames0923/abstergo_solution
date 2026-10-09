package com.tbs.web.gate;

import com.tbs.common.IStartup;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import com.tbs.web.gate.center.IGateWayCenter;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * 令牌处理过滤器
 * <p>
 * 作为请求入口的统一令牌处理点：遍历所有已注册的 {@link IGateWayCenter}（按 {@code Ordered} 排序），
 * 由支持当前路径的管理器提取并校验令牌、构造认证上下文；任一管理器抛紧急异常则直接写错误响应中断请求，
 * 非紧急异常仅告警放行。启动时通过 {@link #startUp} 收集全部管理器并按序排序。
 * </p>
 *
 * @author jiangnan
 * @see IGateWayCenter 网关中心契约
 * @see IStartup 启动初始化钩子
 */
@Slf4j
public class TokenHandleFilter extends OncePerRequestFilter implements IStartup {

    private Collection<IGateWayCenter> securityManagers = new LinkedList<>();

    @Resource
    private ITokenIssuanceCenter tokenIssuanceCenter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        for (IGateWayCenter manager : securityManagers) {
            if (!manager.support(request)) {
                continue;
            }
            try {
                String token = manager.getTokenPicker().pick(request);
                ITokenModel model = tokenIssuanceCenter.getTokenByToken(token);
                if (model == null || StrUtil.isEmpty(model.getToken())) {
                    manager.onNoneToken();
                    continue;
                }
                manager.handleToken(model);
            } catch (Throwable e) {
                boolean willBroken = manager.onErrorWillBroken(e);
                if (willBroken) {
                    AjaxResult result = AjaxUtil.getInstance().createAjaxResultOnError(e);
                    AjaxUtil.writeInfoResp(response, result);
                    return;
                } else {
                    log.warn("安全管理器处理非紧急异常。详情：{}", e.getLocalizedMessage());
                }
            }

        }

        filterChain.doFilter(request, response);
    }

    @Override
    public void startUp() throws RuntimeException {
        try {
            this.securityManagers.addAll(SpringUtil.getBeansOfType(IGateWayCenter.class).values());
            this.securityManagers = this.securityManagers.stream().sorted((o1, o2) -> o1.getOrder() - o2.getOrder())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("init security manager error", e);
        }
        log.info("security manager size: {}", this.securityManagers.size());
    }
}
