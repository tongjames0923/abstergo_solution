package com.tbs.app.controller.publics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tbs.app.config.condition.ConditionalOnTestControllerMode;
import com.tbs.app.config.property.TestControllerMode;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.RegistryEntry;
import com.tbs.common.provider.ResolvedObject;
import com.tbs.common.provider.RoutingStrategy;
import com.tbs.common.util.base.StrUtil;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

/**
 * 统一注册中心 / 多来源路由整体功能测试控制器。
 * <p>
 * 以 {@link IDataDicService}（{@code @RoutingService} 接口）为观察对象、dic-service 为 Dubbo 实例，
 * 免认证暴露三个只读探针，串起阶段 3 接线后的完整链路：
 * <ol>
 *     <li>{@code /discovery}：同一接口分别经旧解析链（{@link IObjectProvider#getObjects}）与新注册中心
 *         （{@link IRegistryCenter#resolve}）枚举，比对来源/坐标/实例标识是否一致，并演示按 business 定向命中；</li>
 *     <li>{@code /resolved}：按指定来源取最优实例的元数据（Dubbo 来源为负载均衡整体引用）；</li>
 *     <li>{@code /call}：经路由代理实调 dic-service 的字典查询，验证端到端转发真正落到远端并取回数据。</li>
 * </ol>
 * app 侧无 {@code IDataDicService} 本地实现，LOCAL_FIRST 会退化到 Dubbo 来源，正好命中 dic-service。
 * </p>
 * <p>
 * 本控制器仅在测试模式（{@code app.gateway.enable-test-controller} 为 pub/all）下装配，生产关闭。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/routing-test")
@Tag(name = "注册中心路由测试控制器", description = "以 dic-service 为 Dubbo 实例验证统一注册中心与多来源路由整体功能")
@ConditionalOnTestControllerMode({TestControllerMode.ALL, TestControllerMode.PUB})
public class PublicRoutingTestController extends BaseController {

    /**
     * 统一对象提供器（旧多来源解析链门面）
     */
    @Resource
    private IObjectProvider objectProvider;

    /**
     * 统一注册中心（阶段 3 新接线：来源贡献 + 按需懒填充 + 统一解析分派）
     */
    @Resource
    private IRegistryCenter registryCenter;

    /**
     * 数据字典服务：{@code @RoutingService} 自动注册的路由代理 Bean，作为被观察与实调的目标
     */
    @Resource
    private IDataDicService dataDicService;

    /**
     * 发现探针：并列输出解析链枚举与注册中心枚举结果，验证两条链路一致且坐标完整
     *
     * @param business 可选业务分区过滤，非空时演示注册中心按 business 定向命中
     * @return 两条链路的实例视图与已登记接口键集合
     */
    @GetMapping("/discovery")
    @AjaxResponseType(Object.class)
    @Operation(summary = "多来源发现探针", description = "同一接口经解析链与注册中心分别枚举，比对来源/坐标/实例标识")
    public AjaxResult discovery(
        @Parameter(name = "business", description = "业务分区过滤，演示定向命中，可空") String business) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("providerChain", objectProvider.getObjects(IDataDicService.class).stream()
            .map(this::describeResolved).collect(Collectors.toList()));
        view.put("registryCenterAll", registryCenter
            .resolve(IDataDicService.class, null, null, DispatchPolicy.ALL, RoutingStrategy.LOCAL_FIRST).stream()
            .map(this::describeEntry).collect(Collectors.toList()));
        view.put("registryCenterByBusiness", StrUtil.isEmpty(business) ? null : registryCenter
            .resolve(IDataDicService.class, business, null, DispatchPolicy.ALL, RoutingStrategy.DUBBO_ONLY).stream()
            .map(this::describeEntry).collect(Collectors.toList()));
        view.put("registeredKeys", registryCenter.keys().stream().map(Class::getName).collect(Collectors.toList()));
        return successResult(view);
    }

    /**
     * 定向解析探针：按指定来源取最优实例元数据
     *
     * @param source 来源标识（spring / dubbo），空则默认 dubbo
     * @return 该来源最优实例的来源/坐标/实例标识，未命中时 found=false
     */
    @GetMapping("/resolved")
    @AjaxResponseType(Object.class)
    @Operation(summary = "按来源定向解析探针", description = "从指定来源解析单个最优实例（Dubbo 返回负载均衡整体引用）")
    public AjaxResult resolved(
        @Parameter(name = "source", description = "来源标识：spring / dubbo，可空默认 dubbo") String source) {
        ObjectSource target = ObjectSource.DUBBO.id().equalsIgnoreCase(source)
            ? ObjectSource.DUBBO : ObjectSource.SPRING;
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("requestedSource", target.id());
        var hit = objectProvider.getResolvedObject(IDataDicService.class, target);
        view.put("found", hit.isPresent());
        hit.ifPresent(resolved -> view.put("instance", describeResolved(resolved)));
        return successResult(view);
    }

    /**
     * 端到端调用探针：经路由代理实调 dic-service 字典查询
     *
     * @param code     字典编码
     * @param strategy 路由策略名（LOCAL_FIRST/REMOTE_FIRST/SPRING_ONLY/DUBBO_ONLY），空则 LOCAL_FIRST
     * @return 代理命中的来源快照与字典查询返回数据
     */
    @GetMapping("/call")
    @AjaxResponseType(value = List.class, genericArgs = {DicModel.class})
    @Operation(summary = "路由代理端到端实调", description = "经指定策略路由代理调用 dic-service 字典查询，验证转发真正落到远端")
    public AjaxResult call(
        @Parameter(name = "code", description = "字典编码") @RequestParam(defaultValue = "") String code,
        @Parameter(name = "strategy", description = "路由策略名，可空默认 LOCAL_FIRST") String strategy) {
        RoutingStrategy routing = StrUtil.isEmpty(strategy)
            ? RoutingStrategy.LOCAL_FIRST : RoutingStrategy.byName(strategy);
        IDataDicService proxy = registryCenter.getRoutingProxy(IDataDicService.class, routing, null, null);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("strategy", routing.name());
        view.put("primarySource", routing.primarySource().id());
        view.put("sameInstanceAsInjectedBean", proxy == dataDicService);
        List<DicModel> dicResult = new ArrayList<>(proxy.getDicByCode(code, true));
        view.put("dicCode", code);
        view.put("dicModels", dicResult);
        return successResult(view);
    }

    /**
     * 将解析链结果转为可读视图
     *
     * @param resolved 解析结果，不能为null
     * @return 来源/坐标/实例标识描述
     */
    private Map<String, Object> describeResolved(ResolvedObject<?> resolved) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", resolved.getSource().id());
        item.put("business", resolved.getBusiness());
        item.put("version", resolved.getVersion());
        item.put("instanceKey", resolved.getInstanceKey());
        item.put("clusterReference", resolved.isClusterReference());
        return item;
    }

    /**
     * 将注册中心条目转为可读视图
     *
     * @param entry 注册条目，不能为null
     * @return 来源/坐标/实例标识/优先级描述
     */
    private Map<String, Object> describeEntry(RegistryEntry entry) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", entry.coordinate().getSource().id());
        item.put("business", entry.coordinate().getBusiness());
        item.put("version", entry.coordinate().getVersion());
        item.put("instanceKey", entry.instanceKey());
        item.put("order", entry.order());
        return item;
    }

    @Override
    public String getBusinessName() {
        return "注册中心路由测试";
    }
}
