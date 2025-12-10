package com.tbs.app.service;

import com.tbs.web.trace.annotation.Traced;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 测试服务类
 * <p>
 * 用于系统功能测试的服务类，提供测试方法
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Service
public class TestService {

    @Resource
    Test2Service test2Service;

    /**
     * 测试方法
     * <p>
     * 调用Test2Service的test方法并返回测试结果
     * </p>
     *
     * @return 测试结果字符串
     */
    @Traced
    public String test() {
        test2Service.test();
        return "test";
    }
}
