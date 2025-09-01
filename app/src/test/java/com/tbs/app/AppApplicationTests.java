package com.tbs.app;

import com.tbs.app.service.business.department.IDepartmentService;
import com.tbs.app.service.business.user.ISystemUserService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.broadcast.enums.HandleMode;
import com.tbs.broadcast.event.impl.SimpleEvent;
import com.tbs.broadcast.publisher.IPublisher;
import com.tbs.common.util.framework.DelegateUtil;
import com.tbs.schedule.impl.TimingWheelSchedule;
import com.tbs.shared.pojo.model.PageVO;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@SpringBootTest(classes = AppApplication.class)
class AppApplicationTests {

	private Logger logger = LoggerFactory.getLogger(AppApplicationTests.class);

	@Resource
	private ISystemUserService systemUserService;

	@Resource
	IPublisher publisher;

	// @Resource(name = RedisBeanNameConstants.DEFAULT_REDIS_TEMPLATE)
	// RedisTemplate<String, Object> redisTemplate;

	@Test
	void boardcastTest() throws ExecutionException, InterruptedException {
		var h = publisher.publish(new SimpleEvent("test", "test"), HandleMode.ASYNC);
		logger.info("publish finish");
		Thread.currentThread().join(10000);

		logger.info("handle finish");
	}

	// @Test
	// void redisTest() {
	// boolean r = redisTemplate.expireAt("user_role::1", new
	// Date(System.currentTimeMillis() + 10000));
	// logger.info("{}", r);
	// }

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

	@Resource
	private TimingWheelSchedule timingWheelSchedule;

	@Test
	void timingWheelTest() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);

		timingWheelSchedule.pin(() -> {
			logger.info("task run");
			latch.countDown();
		}, Duration.ofMillis(2400));
		logger.info("start timing wheel test");
		latch.await();
		logger.info("timing wheel test finish");
	}

	@Resource
	IDepartmentService departmentService;

	@Test
	void test() throws Exception {
		for (int i = 0; i < 5; i++) {
			SystemDepartmentEntity department = new SystemDepartmentEntity();
			department.setDepartmentName("测试公司" + (char)('A' + i));
			department.setDescription("用于测试的公司");
			departmentService.save(department);
		}
	}

	@Test
	void test1() {
		for (int i = 0; i < 3; i++) {
			SystemDepartmentEntity department = new SystemDepartmentEntity();
			department.setDepartmentName("测试部门" + (char)('A' + i));
			department.setDescription("用于测试的公司");
			department.setParentId(39L);
			departmentService.save(department);
		}
	}

}
