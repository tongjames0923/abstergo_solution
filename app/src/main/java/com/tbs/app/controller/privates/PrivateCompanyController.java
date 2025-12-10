package com.tbs.app.controller.privates;

import com.tbs.app.service.company.ICompanyUserRelationService;
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
 * 私有公司控制器
 * <p>
 * 提供需要登录才能访问的公司相关接口，包括获取用户可访问的公司列表
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/company")
@PreAuthorize(value = "#authUtil.userLoggedIn")
public class PrivateCompanyController extends BaseController {

    @Resource
    private ICompanyUserRelationService relationService;

    /**
     * 获取用户可访问公司列表
     * <p>
     * 根据用户ID查询该用户可以访问的所有公司信息
     * </p>
     *
     * @param userId 用户ID
     * @return 用户可访问的公司列表
     */
    @GetMapping("avaliable/{userId}")
    public AjaxResult avaliable(@PathVariable Long userId) {
        return AjaxUtil.getInstance().success(relationService.queryUserCompanies(userId));
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
        return "常规公司功能";
    }
}
