package com.tbs.app.controller.publics;

import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class PublicTestController extends BaseController {
    @GetMapping("/test")
    public AjaxResult test() {
        return AjaxUtil.getInstance().success(null);
    }
}
