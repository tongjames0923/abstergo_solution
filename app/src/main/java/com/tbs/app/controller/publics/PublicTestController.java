package com.tbs.app.controller.publics;

import com.alibaba.nacos.api.config.ConfigService;
import com.tbs.common.util.base.StrUtil;
import com.tbs.app.config.condition.ConditionalOnTestControllerMode;
import com.tbs.app.config.property.TestControllerMode;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.script.api.compiler.impl.SpelCompiler;
import com.tbs.script.api.engine.IScriptEngine;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;

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

    @DubboReference
    private ITreeStore store;

    @Resource
    private ConfigService configService;

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
        return AjaxUtil.getInstance().success(store.getDirectChildren(nodeId, TEST_GROUP));
    }

    @PostMapping("/getParentNodes")
    @AjaxResponseType(Object.class)
    public AjaxResult getParentNodes(String nodeId) {
        return AjaxUtil.getInstance().success(store.getParentNodes(nodeId, TEST_GROUP));
    }

    @PostMapping("/deleteNode")
    @AjaxResponseType(void.class)
    public AjaxResult deleteNode(String nodeId) {
        store.delete(nodeId, TEST_GROUP);
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

    //	@Resource
    //	TestService service;
    //
    //	@Cacheable(key = "#value", value = "test")
    //	@ExpireOn(value = 5)
    //	@GetMapping("/test")
    //	@Operation(summary = "测试缓存", description = "测试缓存功能")
    //	@ApiResponse(responseCode = "200", description = "测试成功",
    //		content = @Content(mediaType = "application/json",
    //			schema = @Schema(implementation = AjaxResult.class)))
    //	public AjaxResult test(@Parameter(name = "value", description = "测试值") String value) throws InterruptedException {
    //		return AjaxUtil.getInstance().success(service.test());
    //	}
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
