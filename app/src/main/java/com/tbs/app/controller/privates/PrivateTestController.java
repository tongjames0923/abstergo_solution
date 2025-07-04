package com.tbs.app.controller.privates;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/private")
public class PrivateTestController extends BaseController {
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/test")
    public AjaxResult test() {
        return AjaxUtil.getInstance().success(null);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/testAuth")
    public AjaxResult testAuth() {
        return AjaxUtil.getInstance().success(null);
    }
}
