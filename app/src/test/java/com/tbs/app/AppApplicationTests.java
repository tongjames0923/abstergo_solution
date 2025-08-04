package com.tbs.app;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.boardcast.enums.HandleMode;
import com.tbs.boardcast.event.impl.SimpleEvent;

import com.tbs.boardcast.publisher.IPublisher;
import com.tbs.common.util.framework.DelegateUtil;
import com.tbs.redis.constants.RedisBeanNameConstants;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

    private Logger logger = LoggerFactory.getLogger(AppApplicationTests.class);

    @Resource
    private ISystemUserService systemUserService;

    @Resource
    IPublisher publisher;

    @Resource(name = RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
    RedisTemplate<String, Object> redisTemplate;

    @Test
    void boardcastTest() throws ExecutionException, InterruptedException {
        var h = publisher.publish(new SimpleEvent("test", "test"), HandleMode.ASYNC);
        logger.info("publish finish");
        logger.info("handle finish");
    }

    @Test
    void redisTest() {
        boolean r = redisTemplate.expireAt("user_role::1", new Date(System.currentTimeMillis() + 10000));
        logger.info("{}", r);
    }

    @Test
    void pageInitTest() {
        PageVO vo = new PageVO<Integer>();
        var all = DelegateUtil.getInstance().delegateWithTarget(vo).build(AsyncDelegateInvoker.class)
            .takeIn(PageVO::setData, () -> Arrays.asList(1, 2, 3)).takeIn(PageVO::setTotal, () -> {
                try {
                    Thread.currentThread().join(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return 10L;
            }).executeAll();
        logger.info("start page init");
        all.forEach(flag -> {
            flag.waitUntilFinished().expceptionThenThrow().get();
        });
        logger.info("page init finish");
    }

}
