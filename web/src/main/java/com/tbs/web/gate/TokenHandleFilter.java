package com.tbs.web.gate;

import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.gate.center.IGateWayCenter;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.uitl.AjaxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * 令牌处理过滤器
 *
 * @author jiangnan
 *
 */
@Slf4j
public class TokenHandleFilter extends OncePerRequestFilter {

    private Collection<IGateWayCenter> securityManagers = new LinkedList<>();

    @Resource
    ITokenIssuanceCenter tokenIssuanceCenter;

    @PostConstruct
    void init() {
        try {
            this.securityManagers.addAll(SpringUtil.getBeansOfType(IGateWayCenter.class).values());
            this.securityManagers = this.securityManagers.stream().sorted((o1, o2) -> o1.getOrder() - o2.getOrder())
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("init security manager error", e);
        }
        log.info("security manager size: {}", this.securityManagers.size());
    }

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
}
