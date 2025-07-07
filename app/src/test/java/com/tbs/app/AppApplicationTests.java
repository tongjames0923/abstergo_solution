package com.tbs.app;

import com.tbs.boardcast.event.impl.SimpleEventImpl;
import com.tbs.boardcast.publisher.IPublishResult;
import com.tbs.boardcast.publisher.IPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

    @Resource
    private IPublisher publisher;

    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        List<IPublishResult<?>> task = new ArrayList<>();
        task.add(publisher.pushlishAsync(new SimpleEventImpl<>("1", "test")));
        task.add(publisher.pushlishAsync(new SimpleEventImpl<>("2", "test")));
        task.add(publisher.pushlishAsync(new SimpleEventImpl<>("3", "test")));
        task.add(publisher.pushlishSync(new SimpleEventImpl<>("4", "test")));
        for (IPublishResult<?> result : task) {
            result.getEvent().untilEventComplete();
        }
    }

}
