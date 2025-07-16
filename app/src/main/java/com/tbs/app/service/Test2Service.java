package com.tbs.app.service;

import com.tbs.web.trace.annotation.Traced;
import org.springframework.stereotype.Service;

/**
 * @author abstergo
 */
@Service
public class Test2Service {

    @Traced
    public void test() {
        try {
            Thread.currentThread().join(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
