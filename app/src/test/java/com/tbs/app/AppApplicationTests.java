package com.tbs.app;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.common.delegate.DelegateMethodExecutor;
import com.tbs.common.delegate.IDelegateProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {


    private Logger logger = LoggerFactory.getLogger(AppApplicationTests.class);

    @Resource
    private ISystemUserService systemUserService;

    @Resource
    IDelegateProvider delegateProvider;

    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        PageVO<Integer> vo = new PageVO<>();
        var exec = new DelegateMethodExecutor<>(delegateProvider.getDelegate(vo), PageVO::setTotal, () -> {
            try {
                Thread.currentThread().join(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 100L;
        }).execute();

        logger.info("exec result {}", exec);

    }

}
