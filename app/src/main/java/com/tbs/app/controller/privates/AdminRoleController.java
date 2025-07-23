package com.tbs.app.controller.privates;

import com.tbs.app.constant.CommonConstant;
import com.tbs.app.enums.SystemUserStatusEnum;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.app.service.business.IUserRoleRelationService;
import com.tbs.common.error.AssertUtil;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/private/admin")
@RestController
public class AdminRoleController extends BaseController {

    @Resource
    private IUserRoleRelationService relationService;

    @PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
    @GetMapping("/role/enable/{userId}")
    public AjaxResult enable(@PathVariable Long userId, String role) {
        relationService.enableRole(userId, role);
        return AjaxUtil.getInstance().success("授权成功", null);
    }

    @PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
    @GetMapping("/role/disable/{userId}")
    public AjaxResult disable(@PathVariable Long userId, String role) {
        relationService.disableRole(userId, role);
        return AjaxUtil.getInstance().success("取消授权成功", null);
    }

    @Resource
    private ISystemUserService userService;

    @PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
    @GetMapping("/account/activite/{userId}")
    public AjaxResult activiteAccount(@PathVariable Long userId) {
        SystemUser user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        user.setFlag(SystemUserStatusEnum.ACTIVITE.getCode());
        userService.save(user);
        return AjaxUtil.getInstance().success("激活成功", null);
    }

    @PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
    @GetMapping("/account/disable/{userId}")
    public AjaxResult disableAccount(@PathVariable Long userId) {
        SystemUser user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        user.setFlag(SystemUserStatusEnum.DISABLED.getCode());
        userService.save(user);
        return AjaxUtil.getInstance().success("禁用成功", null);
    }
}
