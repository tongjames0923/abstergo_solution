package com.tbs.app;

import com.tbs.async.util.ThreadUtil;
import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.common.util.HolderUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

    @Resource
    private IPublisher publisher;

    private Logger logger = LoggerFactory.getLogger(AppApplicationTests.class);
    @Test
    void contextLoads() throws ExecutionException, InterruptedException {

        var context1 = HolderUtil.getDefaultLocalStoredContext();
        var tasks = ThreadUtil.getInstance().createAsyncCall(context1).run((context, param) -> {
            HolderUtil.put(context, "test", param);
        }, () -> {
            try {
                Thread.currentThread().join(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "XXX";
        }).buildAndExecuteAsync();
        for (var task : tasks) {
            task.get();
        }
        logger.info("{}", HolderUtil.get(context1, "test"));
    }

}
