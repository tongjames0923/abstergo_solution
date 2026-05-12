package com.tbs.app.controller.privates;

import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 私有部门控制器
 * <p>
 * 提供需要登录才能访问的部门相关接口，包括获取用户可访问的部门列表
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/department")
@PreAuthorize(value = "#authUtil.userLoggedIn")
public class PrivateDepartmentController extends BaseController {

    @Resource
    private IDepartmentUserRelationService relationService;

    /**
     * 获取用户可访问部门列表
     * <p>
     * 根据用户ID查询该用户可以访问的所有部门信息
     * </p>
     *
     * @param userId 用户ID
     * @return 用户可访问的部门列表
     */
    @GetMapping("avaliable/{userId}")
    public AjaxResult avaliable(@PathVariable Long userId) {
        return AjaxUtil.getInstance().success(relationService.queryUserDepartments(userId));
    }

    /**
     * 获取业务名称
     * <p>
     * 实现BaseController接口的方法，返回当前控制器的业务名称
     * </p>
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "常规部门功能";
    }
}
