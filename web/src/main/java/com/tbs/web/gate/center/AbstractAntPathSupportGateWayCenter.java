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
 * 抽象的基于路径匹配的网关中心
 * <p>
 * 以 Ant 风格路径集合配置受保护路径，封装 {@link RequestMatcher} 匹配逻辑：
 * {@link #setMatcher} 将路径列表编译为 {@code OrRequestMatcher}，{@link #support} 据此判断请求是否命中。
 * 具体令牌解析与上下文构造交由子类实现。
 * </p>
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
        // 过滤空路径后编译为 Ant 匹配器，再用 Or 组合成整体匹配规则
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
        // 无匹配器（未配置路径）时视为不支持，交由其它中心处理
        return matcher.matches(request);
    }
}
