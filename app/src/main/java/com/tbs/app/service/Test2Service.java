package com.tbs.app.service;

import com.tbs.web.trace.annotation.Traced;
import org.springframework.stereotype.Service;

/**
 * 测试服务类2
 * <p>
 * 用于系统功能测试的第二个服务类，提供带延迟的测试方法
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Service
public class Test2Service {

    /**
     * 测试方法
     * <p>
     * 执行一个带有2秒延迟的测试操作
     * </p>
     *
     * @throws RuntimeException 如果线程中断时抛出异常
     */
    @Traced
    public void test() {
        try {
            Thread.currentThread().join(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
