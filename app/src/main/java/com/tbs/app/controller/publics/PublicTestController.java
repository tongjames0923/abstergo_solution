package com.tbs.app.controller.publics;

import com.tbs.app.pojo.criteria.CommonSystemUserQueryCriteria;
import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.app.service.TestService;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.sql.model.QueryPageModel;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author abstergo
 */
@RestController
@RequestMapping("/public")
public class PublicTestController extends BaseController {

    @Resource
    TestService service;
    @Cacheable(key = "#value",value = "test")
    @ExpireOn(value = 5)
    @GetMapping("/test")
    public AjaxResult test(String value) throws InterruptedException {
        return AjaxUtil.getInstance().success(service.test());
    }

    @GetMapping("/test1")
    public AjaxResult test1() throws InterruptedException {
        return AjaxUtil.getInstance().success(1);
    }

    @Resource
    private ISystemUserService userService;

    @PostMapping("saveUser")
    public AjaxResult createUser(@RequestBody SystemUser systemUser) {
        return AjaxUtil.getInstance().success("保存成功", userService.save(systemUser));
    }

    @PostMapping("queryUser")
    public AjaxResult queryUser(@RequestBody CommonSystemUserQueryCriteria criteria, long pageNum, long pageSize) {
        return AjaxUtil.getInstance()
            .success("查询成功", userService.queryBy(criteria, QueryPageModel.of(pageNum, pageSize)));
    }

//    @Resource
//    private IUserAuthService userAuthService;

//    @GetMapping("/login")
//    public AjaxResult login(Long userId, String password) {
//        return AjaxUtil.getInstance().success(userAuthService.login(String.valueOf(userId), password));
//    }
}
