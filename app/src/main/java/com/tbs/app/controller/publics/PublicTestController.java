package com.tbs.app.controller.publics;

import com.tbs.app.service.TestService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

//    @Resource
//    private IUserAuthService userAuthService;

//    @GetMapping("/login")
//    public AjaxResult login(Long userId, String password) {
//        return AjaxUtil.getInstance().success(userAuthService.login(String.valueOf(userId), password));
//    }
}
