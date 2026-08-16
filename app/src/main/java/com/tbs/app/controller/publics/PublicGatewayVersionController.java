package com.tbs.app.controller.publics;

import com.tbs.app.config.property.GatewayProperty;
import com.tbs.view.route.api.IViewRouteService;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 网关版本控制器
 * <p>
 * 提供网关（app模块）的版本信息查询接口，用于：
 * <ul>
 *     <li>蓝绿部署切换后验证流量是否已到达新版本实例</li>
 *     <li>运维巡检确认各实例运行的构建版本与构建时间</li>
 * </ul>
 * 版本信息解析优先级（保证IDE直跑时同样可用）：
 * <ol>
 *     <li>启动参数/环境变量 APP_GATEWAY_VERSION（显式覆盖，如 -DAPP_GATEWAY_VERSION=1.2.3-hotfix）</li>
 *     <li>Maven打包时 build-info goal 生成的 META-INF/build-info.properties（含构建时间）</li>
 *     <li>Maven资源过滤注入 application.yml 的 pom 版本号（IDE编译时同样生效）</li>
 *     <li>均未命中时返回 unknown</li>
 * </ol>
 * </p>
 * <p>
 * 本接口位于 /public/** 白名单路径下，无需认证即可访问。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/gateway")
@Tag(name = "网关版本控制器", description = "查询网关服务版本信息，用于蓝绿部署验证与运维巡检")
public class PublicGatewayVersionController {

    /**
     * 网关配置属性
     */
    @Resource
    private GatewayProperty gatewayProperty;

    /**
     * 构建信息（由 build-info goal 生成，IDE 运行时可能不存在）
     */
    @Resource
    private ObjectProvider<BuildProperties> buildPropertiesProvider;

    /**
     * 当前实例服务端口（蓝绿部署时用于区分请求落在哪个实例）
     */
    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 视图路由服务（可选注入，YAML实现时视图版本返回0）
     */
    @Resource
    private ObjectProvider<IViewRouteService> viewRouteServiceProvider;

    /**
     * 查询网关版本信息
     * <p>
     * 返回构建版本、构件名称、构建时间以及当前实例端口。 蓝绿切换验证示例：
     * <pre>curl http://127.0.0.1:8082/public/gateway/version</pre>
     * 返回的版本号/构建时间与发布包一致即说明流量已到达新版本。
     * </p>
     *
     * @return 版本信息
     */
    @GetMapping("/version")
    @Operation(summary = "查询网关版本", description = "返回网关构建版本、构建时间、实例端口与视图路由配置版本，无需认证")
    public AjaxResult version() {
        Map<String, String> info = new LinkedHashMap<>();
        BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
        info.put("version", resolveVersion(buildProperties));
        info.put("artifact", buildProperties != null ? buildProperties.getArtifact() : "app");
        // 构建时间仅 Maven 打包后存在（来自 build-info）；IDE 运行时无该信息，返回 unknown
        info.put("buildTime", buildProperties != null ? String.valueOf(buildProperties.getTime()) : UNKNOWN);
        info.put("instancePort", serverPort);
        // 视图路由配置版本（版本表MAX(id)），前端据此与本地版本比对判断菜单是否需要更新
        IViewRouteService viewRouteService = viewRouteServiceProvider.getIfAvailable();
        info.put("viewVersion", viewRouteService != null ? String.valueOf(viewRouteService.getViewVersion()) : "0");
        return AjaxUtil.getInstance().success(info);
    }

    /**
     * 解析版本号
     * <p>
     * 优先级：配置化版本（含启动参数覆盖）&gt; build-info &gt; unknown。 若资源过滤未执行（值仍是 @project.version@ 占位符原文）则视为无效。
     * </p>
     *
     * @param buildProperties 构建信息，可能为null
     * @return 版本号
     */
    private String resolveVersion(BuildProperties buildProperties) {
        // 1. 配置化版本：启动参数覆盖或资源过滤注入的pom版本（IDE运行时也有效）
        String version = gatewayProperty.getVersion();
        if (version != null && !version.isEmpty() && !version.contains("@")) {
            return version;
        }
        // 2. Maven打包生成的build-info
        if (buildProperties != null) {
            return buildProperties.getVersion();
        }
        return UNKNOWN;
    }

    /**
     * 无法获取信息时的占位值
     */
    private static final String UNKNOWN = "unknown";
}
