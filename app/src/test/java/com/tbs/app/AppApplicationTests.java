package com.tbs.app;

import cn.hutool.core.date.DateUtil;
import com.tbs.app.pojo.CommonSystemUserQueryCriteria;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.sql.model.QueryPageModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {


    private Logger logger = LoggerFactory.getLogger(AppApplicationTests.class);

    @Resource
    private ISystemUserService systemUserService;
    @Test
    void contextLoads() throws ExecutionException, InterruptedException {
        Date now = new Date();
        Date last3Day = DateUtil.offsetDay(now, -3);
        var p = systemUserService.queryBy(
            new CommonSystemUserQueryCriteria().setUserNameEquals("1196489944").setCreateTimeGreaterThan(last3Day),
            QueryPageModel.of(1L, 10L));
        logger.info("{}", p);
    }

}
