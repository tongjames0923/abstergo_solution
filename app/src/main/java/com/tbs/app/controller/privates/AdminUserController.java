package com.tbs.app.controller.privates;

import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.shared.interfaces.AbstractCrudController;
import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.criteria.CommonSystemUserQueryCriteria;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.security.issuance.center.ITokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.user.IUserLoginInfoService;
import com.tbs.web.security.user.LoginSessionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户管理控制器
 * <p>
 * 该控制器用于管理系统管理员用户，提供用户的基本增删改查操作
 * </p>
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/user")
@PreAuthorize("hasAuthority('" + RoleConstants.SUPER_ADMIN_ROLE_ID + "')")
@Api(value = "管理员用户管理", tags = "管理员用户管理接口")
public class AdminUserController extends AbstractCrudController<SystemUserEntity, Long, CommonSystemUserQueryCriteria> {
    @Resource
    private ISystemUserService service;

    @Resource
    IUserLoginInfoService loginInfoService;

    @Resource
    private ITokenIssuanceCenter center;

    @Override
    public IServiceTemplate<SystemUserEntity, Long> getService() {
        return service;
    }

    /**
     * 根据ID获取管理员用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    @GetMapping("get/{id}")
    @ApiOperation(value = "获取管理员用户", notes = "根据用户ID获取管理员用户详细信息")
    public AjaxResult get(@ApiParam(name = "id", value = "用户ID", required = true) @PathVariable Long id) {
        return super.get(id);
    }

    /**
     * 保存管理员用户信息
     *
     * @param entity 用户实体对象
     * @return 保存后的用户信息
     */
    @Override
    @PostMapping("save")
    @ApiOperation(value = "保存管理员用户", notes = "创建或更新管理员用户信息")
    public AjaxResult save(
        @ApiParam(name = "entity", value = "用户信息", required = true) @RequestBody SystemUserEntity entity) {
        return super.save(entity);
    }

    /**
     * 删除管理员用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @Override
    @PostMapping("delete/{id}")
    @ApiOperation(value = "删除管理员用户", notes = "根据用户ID删除管理员用户")
    public AjaxResult delete(@ApiParam(name = "id", value = "用户ID", required = true) @PathVariable Long id) {
        return super.delete(id);
    }

    /**
     * 查询管理员用户列表
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 用户列表分页结果
     */
    @Override
    @PostMapping("query")
    @ApiOperation(value = "查询管理员用户", notes = "根据条件查询管理员用户列表，支持分页")
    public AjaxResult query(
        @ApiParam(name = "criteria", value = "查询条件") @RequestBody CommonSystemUserQueryCriteria criteria,
        @ApiParam(name = "pageNum", value = "页码") @RequestParam(required = false) Long pageNum,
        @ApiParam(name = "pageSize", value = "每页大小") @RequestParam(required = false) Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    /**
     * 获取用户登录会话列表
     *
     * @param id 用户ID
     * @return 用户登录会话列表
     */
    @GetMapping("sessions/{id}")
    public List<LoginSessionVO> getUserSessions(@PathVariable Long id) {
        var user = getService().queryById(id);
        AssertUtil.notNull(user, "用户不存在");
        return loginInfoService.listUserSessions(user.getUserName());
    }

    /**
     * 获取用户登录会话列表的Token数据
     *
     * @param sessionId 用户登录会话ID
     * @return 用户登录会话列表
     */
    @GetMapping("tokens/{sessionId}")
    public List<ITokenModel> listTokensBySessionId(@PathVariable String sessionId) {
        return center.getTokensBySessionId(sessionId).stream().collect(Collectors.toList());
    }

    @Override
    public String getBusinessName() {
        return "管理员用户管理";
    }
}
