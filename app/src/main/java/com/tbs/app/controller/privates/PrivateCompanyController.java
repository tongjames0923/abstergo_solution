package com.tbs.app.controller.privates;

import com.tbs.app.service.business.company.ICompanyUserRelationService;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/private/company")
@PreAuthorize(value = "#authUtil.userLoggedIn")
public class PrivateCompanyController extends BaseController {

    @Resource
    private ICompanyUserRelationService relationService;

    /**
     * 获取用户可访问公司列表
     *
     * @param userId
     * @return
     */
    @GetMapping("avaliable/{userId}")
    public AjaxResult avaliable(@PathVariable Long userId) {
        return AjaxUtil.getInstance().success(relationService.queryUserCompanies(userId));
    }


    @Override
    public String getBusinessName() {
        return "常规公司功能";
    }
}
