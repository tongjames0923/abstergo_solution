package com.tbs.app;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.async.constant.AsyncBeanNameConstant;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.impl.CallerRunsDelegateTarget;
import com.tbs.common.delegate.impl.CompletableFutureFlagProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

    private Logger logger = LoggerFactory.getLogger(AppApplicationTests.class);

    @Resource
    private ISystemUserService systemUserService;

    @Resource(name = AsyncBeanNameConstant.MAIN_EXECUTOR_SERVICE)
    ExecutorService executorService;

    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        PageVO vo = new PageVO<Integer>();
        IDelegateInvoker<PageVO> invoker =
            new AsyncDelegateInvoker<>(new CallerRunsDelegateTarget<>(vo), new CompletableFutureFlagProvider(),
                executorService);
        var all = invoker.takeIn(PageVO::setTotal, () -> {
            logger.info("start 1");
            try {
                Thread.currentThread().join(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("end 1");
            return 100L;
        }).takeIn(PageVO::setData, () -> {
            logger.info("start 2");
            try {
                Thread.currentThread().join(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            logger.info("end 2");
            return Arrays.asList(1);
        }).executeAll();
        for (var flag : all) {
            logger.info("flag {}", flag.getUnitlFinished().getTarget());
        }
    }

}
