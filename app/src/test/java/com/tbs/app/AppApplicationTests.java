package com.tbs.app;

import com.tbs.common.util.HolderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

    @Test
    void contextLoads() {
        HolderUtil.put(HolderUtil.getDefaultLocalStoredContext(), "joke", "test");
        System.out.println(HolderUtil.get(HolderUtil.getDefaultLocalStoredContext(), "joke"));

    }

}
