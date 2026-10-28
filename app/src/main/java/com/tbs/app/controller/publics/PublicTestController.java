package com.tbs.app.controller.publics;

import com.alibaba.nacos.api.config.ConfigService;
import com.tbs.app.config.condition.ConditionalOnTestControllerMode;
import com.tbs.app.config.property.TestControllerMode;
import com.tbs.common.annotation.DynamicConfig;
import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.provider.IRegisterContributor;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.vo.InstanceProxy;
import com.tbs.common.util.base.StrUtil;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.script.api.compiler.impl.SpelCompiler;
import com.tbs.script.api.engine.IScriptEngine;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共测试控制器
 * <p>
 * 提供公共访问的测试接口，用于测试系统功能
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public")
@Tag(name = "公共测试控制器", description = "公共测试控制器")
@ConditionalOnTestControllerMode({TestControllerMode.ALL, TestControllerMode.PUB})
public class PublicTestController extends BaseController {

    /**
     * i18n key：探针接口类型无法解析
     */
    private static final String I18N_PROBE_TYPE_NOT_FOUND = "provider.probe.type.not-found";

    @DubboReference
    private ITreeStore treeStore;

    @Resource
    private ConfigService configService;

    /**
     * 注册中心
     * <p>
     * 贡献者由容器注入中心，探针经中心读取贡献者链并取首命中实例。
     * </p>
     */
    @Resource
    private IRegistryCenter registryCenter;

    @SuppressWarnings("unchecked")
    private ITreeStore getTreeStore() {
        return treeStore;
    }

    public static final String TEST_GROUP = "Test Tree Group";

    /**
     * 树节点表单
     * <p>
     * 用于接收创建树节点的请求参数
     * </p>
     */
    @Data
    public static final class TreeForm implements Serializable {
        /**
         * 节点名称
         */
        private String name;

        /**
         * 父节点ID
         */
        private String parentId;

    }

//    /**
//     * 创建树节点
//     * <p>
//     * 根据表单参数创建一个新的树节点
//     * </p>
//     *
//     * @param form 树节点表单参数
//     * @return 创建结果
//     */
//    @PostMapping("/createNode")
//    public AjaxResult createNode(@RequestBody TreeForm form) {
//        store.store(new SimpleTreeStrorableModel(TEST_GROUP, form.getName()), form.getParentId());
//        return AjaxUtil.getInstance().success(null);
//    }

    /**
     * 获取子节点列表
     * <p>
     * 根据节点ID获取所有子节点
     * </p>
     *
     * @param nodeId 节点ID
     * @return 子节点列表
     */
    @PostMapping("/getChildren")
    @AjaxResponseType(Object.class)
    public AjaxResult getChildren(String nodeId) {
        return AjaxUtil.getInstance().success(getTreeStore().getDirectChildren(nodeId, TEST_GROUP));
    }

    @PostMapping("/getParentNodes")
    @AjaxResponseType(Object.class)
    public AjaxResult getParentNodes(String nodeId) {
        return AjaxUtil.getInstance().success(getTreeStore().getParentNodes(nodeId, TEST_GROUP));
    }

    @PostMapping("/deleteNode")
    @AjaxResponseType(void.class)
    public AjaxResult deleteNode(String nodeId) {
        getTreeStore().delete(nodeId, TEST_GROUP);
        return AjaxUtil.getInstance().success(null);
    }

    @Resource
    private IScriptEngine scriptEngine;

    @Data
    public static class SpelForm {
        /**
         * 脚本内容
         */
        private String script;

        private String resultType;
    }

    /**
     * 脚本测试
     * <p>
     * 测试脚本引擎执行脚本
     * </p>
     *
     * @param script 脚本内容
     * @return 执行结果
     */
    @PostMapping("/scriptTest")
    @AjaxResponseType(Object.class)
    public AjaxResult scriptTest(@RequestBody SpelForm script) {
        var spec = scriptEngine.getCompilerSpecWithVersion(SpelCompiler.COMPILER_NAME, SpelCompiler.MAJOR_VERSION,
            SpelCompiler.MINOR_VERSION).orElseThrow(() -> new RuntimeException("不支持的脚本编译器"));
        SpelCompiler compiler = (SpelCompiler)spec.getCompiler();
        if (StrUtil.isNotEmpty(script.getResultType())) {
            try {
                compiler.setResultType(Class.forName(script.getResultType()));
            } catch (ClassNotFoundException e) {

            }
        }
        var executor = spec.compile(script.getScript());
        var result = executor.execute();
        return AjaxUtil.getInstance().success(result);
    }

    @DynamicConfig("tbs.app.test")
    private String test;

    @GetMapping("/test")
    @Operation(summary = "测试缓存", description = "测试缓存功能")
    public AjaxResult test(@Parameter(name = "value", description = "测试值") String value) {
        return AjaxUtil.getInstance().success(test);
    }
    //
    //	@GetMapping("/test1")
    //	@Operation(summary = "简单测试", description = "简单测试功能")
    //	@ApiResponse(responseCode = "200", description = "测试成功",
    //		content = @Content(mediaType = "application/json",
    //			schema = @Schema(implementation = AjaxResult.class)))
    //	public AjaxResult test1() throws InterruptedException {
    //		return AjaxUtil.getInstance().success(1);
    //	}
    //
    //	@Resource
    //	private ISystemUserService userService;
    //
    //	@PostMapping("saveUser")
    //	@Operation(summary = "保存用户", description = "保存用户信息")
    //	@ApiResponse(responseCode = "200", description = "保存成功",
    //		content = @Content(mediaType = "application/json",
    //			schema = @Schema(implementation = AjaxResult.class)))
    //	public AjaxResult createUser(@Parameter(description = "系统用户实体") @RequestBody SystemUserEntity systemUserEntity) {
    //		return AjaxUtil.getInstance().success("保存成功", userService.save(systemUserEntity));
    //	}
    //
    //	@PostMapping("queryUser")
    //	@Operation(summary = "查询用户", description = "根据条件查询用户列表")
    //	@ApiResponse(responseCode = "200", description = "查询成功",
    //		content = @Content(mediaType = "application/json",
    //			schema = @Schema(implementation = AjaxResult.class)))
    //	public AjaxResult queryUser(@Parameter(description = "查询条件") @RequestBody CommonSystemUserQueryCriteria criteria,
    //			@Parameter(name = "pageNum", description = "页码") long pageNum,
    //			@Parameter(name = "pageSize", description = "每页大小") long pageSize) {
    //		return AjaxUtil.getInstance().success("查询成功",
    //				userService.queryAndPage(criteria, QueryPageModel.of(pageNum, pageSize)));
    //	}
    //
    //	@Resource
    //	private ISystemRoleService roleService;
    //
    //	@PostMapping("saveRole")
    //	@Operation(summary = "保存角色", description = "保存角色信息")
    //	@ApiResponse(responseCode = "200", description = "保存成功",
    //		content = @Content(mediaType = "application/json",
    //			schema = @Schema(implementation = AjaxResult.class)))
    //	public AjaxResult createRole(@Parameter(description = "角色实体") @RequestBody SystemRoleEntity role) {
    //		return AjaxUtil.getInstance().success("保存成功", roleService.save(role));
    //	}

    /**
     * 多来源贡献者探针
     * <p>
     * 先经注册中心取贡献者链逐个来源盘点实例，再走 {@code IRegistryCenter#getInstanceProxy} 取首命中，
     * 用于确认注册中心、Spring 贡献者与 Dubbo 贡献者三者已按坐标串通。business/version 留空表示不约束该维度，
     * 填上即可验证坐标收窄是否命中。
     * </p>
     *
     * @param type     接口全限定名，空则取表格数据处理器接口
     * @param business 业务分区，空表示不约束
     * @param version  发布版本，空表示不约束
     * @return 各来源的盘点结果与注册中心的首命中详情
     */
    @GetMapping("/provider/contributors")
    @AjaxResponseType(Object.class)
    @Operation(summary = "多来源贡献者探针", description = "经注册中心逐来源盘点实例并取首命中，验证贡献者链是否生效")
    public AjaxResult contributors(
        @Parameter(name = "type", description = "接口全限定名，空则取表格数据处理器接口") String type,
        @Parameter(name = "business", description = "业务分区，空表示不约束") String business,
        @Parameter(name = "version", description = "发布版本，空表示不约束") String version) {
        Class<?> targetType = resolveProbeType(type);
        List<IRegisterContributor> contributors = registryCenter.getAllContributors();
        List<Map<String, Object>> details = new ArrayList<>(contributors.size());
        for (IRegisterContributor contributor : contributors) {
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("contributorId", contributor.getContributorId());
            detail.put("order", contributor.getOrder());
            try {
                List<InstanceProxy> instances =
                    contributor.getAllInstanceByTypeAndBusinessAndVersion(targetType, business, version);
                detail.put("count", instances.size());
                detail.put("instances", instances.stream().map(this::probeViewOf).toList());
            } catch (Exception e) {
                // 单个来源失败不影响其余来源的盘点结果，失败原因原样带出便于定位
                detail.put("count", 0);
                detail.put("error", e.getMessage());
            }
            details.add(detail);
        }
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("type", targetType.getName());
        view.put("business", business);
        view.put("version", version);
        view.put("contributorIds", contributors.stream().map(IRegisterContributor::getContributorId).toList());
        view.put("contributors", details);
        view.put("centerHit", centerViewOf(registryCenter.getInstanceProxy(targetType, business, version, null)));
        return AjaxUtil.getInstance().success(view);
    }

    /**
     * 组装单个实例的探针视图
     *
     * @param instanceProxy 实例代理，不能为null
     * @return 目标类型全限定名与注册坐标
     */
    private Map<String, Object> probeViewOf(InstanceProxy instanceProxy) {
        Map<String, Object> instanceView = new LinkedHashMap<>();
        instanceView.put("targetTypeFullName", instanceProxy.getTargetTypeFullName());
        instanceView.put("business", instanceProxy.getBusiness());
        instanceView.put("version", instanceProxy.getVersion());
        return instanceView;
    }

    /**
     * 组装注册中心命中视图
     *
     * @param instanceProxy 注册中心返回的实例代理，未命中为null
     * @return 命中标记与命中详情
     */
    private Map<String, Object> centerViewOf(InstanceProxy instanceProxy) {
        Map<String, Object> centerView = new LinkedHashMap<>();
        centerView.put("hit", instanceProxy != null);
        if (instanceProxy == null) {
            return centerView;
        }
        IRegisterContributor contributor = instanceProxy.getContributor();
        centerView.put("contributorId", contributor == null ? null : contributor.getContributorId());
        centerView.put("interfaceFullName", instanceProxy.getInterfaceFullName());
        centerView.putAll(probeViewOf(instanceProxy));
        return centerView;
    }

    /**
     * 解析探针目标接口类型
     *
     * @param type 接口全限定名，空则取表格数据处理器接口
     * @return 目标接口类型
     */
    private Class<?> resolveProbeType(String type) {
        String className = StrUtil.isEmpty(type) ? ITableDataHandler.class.getName() : type;
        Class<?> targetType = null;
        try {
            targetType = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // 类型不存在交由下方断言统一转为业务异常，测试接口不抛受检异常
        }
        AssertUtil.isTrue(targetType == null).i18n(I18N_PROBE_TYPE_NOT_FOUND).params(className).throwIt();
        return targetType;
    }

    @Override
    public String getBusinessName() {
        return "测试";
    }

    // @Resource
    // private IUserAuthService userAuthService;

    // @GetMapping("/login")
    // public AjaxResult login(Long userId, String password) {
    // return
    // AjaxUtil.getInstance().success(userAuthService.login(String.valueOf(userId),
    // password));
    // }
}
