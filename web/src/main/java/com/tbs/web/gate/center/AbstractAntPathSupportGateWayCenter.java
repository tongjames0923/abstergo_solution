package com.tbs.web.gate.center;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import lombok.Getter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象的基于路径匹配的权限管理器
 *
 * @author abstergo
 */
@Getter
public abstract class AbstractAntPathSupportGateWayCenter implements IGateWayCenter {
    RequestMatcher matcher;

    public void setMatcher(Collection<String> antPaths) {
        if (CollUtil.isEmpty(antPaths)) {
            return;
        }
        antPaths = antPaths.stream().filter(p -> {
            return StrUtil.isNotEmpty(p);
        }).collect(Collectors.toList());
        List<RequestMatcher> matchers = new ArrayList<>(antPaths.size());
        matchers.addAll(antPaths.stream().map(path -> new AntPathRequestMatcher(path))
            .collect(java.util.stream.Collectors.toList()));
        matcher = new OrRequestMatcher(matchers);
    }

    @Override
    public boolean support(HttpServletRequest request) {
        return matcher.matches(request);
    }
}
