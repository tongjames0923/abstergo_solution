package com.tbs.app;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.async.constant.AsyncBeanNameConstant;
import com.tbs.common.util.framework.DelegateUtil;
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
        DelegateUtil.getInstance().delegateWithTarget(vo).build(AsyncDelegateInvoker.class)
            .takeIn(PageVO::setData, () -> Arrays.asList(1, 2, 3)).takeIn(PageVO::setTotal, () -> {
                try {
                    Thread.currentThread().join(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return 10L;
            }).executeAll().forEach(flag -> {
                flag.getUnitlFinished().read(PageVO::getData, data -> {
                    logger.info("data {}", data);
                });
            });
    }

}
