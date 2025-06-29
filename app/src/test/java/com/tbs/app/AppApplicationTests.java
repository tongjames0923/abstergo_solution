package com.tbs.app;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.UuidUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

    @Test
    void contextLoads() {
        for (int i = 0; i < 10; i++) {
            System.out.println(UuidUtil.getInstance().getUuidString());
        }
        throw new FormattedRuntimeException(new UnsupportedOperationException("not support"), "");
    }

}
