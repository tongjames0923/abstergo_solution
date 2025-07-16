package com.tbs.app.service;

import com.tbs.web.trace.annotation.Traced;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TestService {

    @Resource
    Test2Service test2Service;

    @Traced
    public String test() {
        test2Service.test();
        return "test";
    }
}
