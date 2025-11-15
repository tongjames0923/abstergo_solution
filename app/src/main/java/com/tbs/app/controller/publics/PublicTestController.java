package com.tbs.app.controller.publics;

import com.tbs.struct.tree.api.persistence.ITreeStore;
import com.tbs.struct.tree.impl.model.SimpleTreeStrorableModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * @author abstergo
 */
@RestController
@RequestMapping("/public")
@Tag(name = "公共测试控制器", description = "公共测试控制器")
public class PublicTestController extends BaseController {

    @Resource
    private ITreeStore store;

    public static final String TEST_GROUP = "Test Tree Group";

    @Data
    public static final class TreeForm implements Serializable {
        private String name;
        private String parentId;

    }

    @PostMapping("/createNode")
    public AjaxResult createNode(@RequestBody TreeForm form) {
        store.store(new SimpleTreeStrorableModel(TEST_GROUP, form.getName()), form.getParentId());
        return AjaxUtil.getInstance().success(null);
    }

    /**
     * 获取子节点
     *
     * @param nodeId
     * @return
     */
    @PostMapping("/getChildren")
    public AjaxResult getChildren(String nodeId) {
        return AjaxUtil.getInstance().success(store.getChildren(nodeId, TEST_GROUP));
    }

    @PostMapping("/getParentNodes")
    public AjaxResult getParentNodes(String nodeId) {
        return AjaxUtil.getInstance().success(store.getParentNodes(nodeId, TEST_GROUP));
    }

    @PostMapping("/deleteNode")
    public AjaxResult deleteNode(String nodeId) {
        store.delete(nodeId, TEST_GROUP);
        return AjaxUtil.getInstance().success(null);
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
