package com.tbs.app.controller.publics;

import com.tbs.app.config.ConnectionAdmissionLimiter;
import com.tbs.app.config.condition.ConditionalOnTestControllerMode;
import com.tbs.app.config.property.TestControllerMode;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 数据库连接并发压测控制器（仅测试用途）。
 * <p>
 * 用 {@code threads} 个虚拟线程在 {@code durationMs} 时长内不停「借连接 → 执行查询 → 归还」，
 * 用于验证 {@code connectionAllowInterceptor} 的连接准入限流：许可数、排队上限、
 * 等待超时三道闸门（均可由 {@code tbs.sql.conn-limit.*} 配置），以及借出/归还的配对语义。
 * 被限流拒绝时抛 {@link FormattedRuntimeException}，据此与真正的 SQL 错误区分统计。
 * </p>
 * <p>
 * 受 {@code app.gateway.enable-test-controller} 开关控制（需为 {@code pub} 或 {@code all} 才注册），
 * 且位于 {@code /public/**} 免鉴权路径，仅供本地/测试环境压测，切勿在生产开启。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/public/db-stress")
@Tag(name = "数据库连接压测控制器", description = "多线程并发读库，验证连接准入限流")
@ConditionalOnTestControllerMode({TestControllerMode.ALL, TestControllerMode.PUB})
@Slf4j
public class PublicDbStressTestController extends BaseController {

    @Resource
    private DataSource dataSource;

    @Resource
    private ConnectionAdmissionLimiter connectionAdmissionLimiter;

    /**
     * 启动贴近真实业务的多线程并发读库压测。
     * <p>
     * 与“死循环忙压”不同：每个虚拟线程模拟一个用户会话，两次请求之间有 {@code thinkMs}
     * 思考时间，并在校验连接持有期间模拟 {@code holdMs} 的查询/业务耗时（真正占用连接），
     * 以此按“到达速率 × 占用时长”形成对连接池并发（许可数 20）的真实压力。
     * </p>
     *
     * @param threads    并发会话（线程）数，范围 [1, 500]，默认 100
     * @param durationMs 每个会话持续压测的时长（毫秒），范围 [100, 60000]，默认 5000
     * @param holdMs     持有连接期间的模拟耗时（毫秒），范围 [0, 2000]，默认 30
     * @param thinkMs    两次请求之间的思考时间（毫秒），范围 [0, 5000]，默认 100
     * @param sql        压测查询语句，默认 {@code SELECT 1}
     * @return 压测汇总结果
     */
    @GetMapping("/read")
    @Operation(summary = "多线程并发读库（模拟真实业务）", description = "带 think-time 与连接占用耗时的并发读库压测")
    public AjaxResult read(
            @RequestParam(value = "threads", defaultValue = "100") int threads,
            @RequestParam(value = "durationMs", defaultValue = "5000") long durationMs,
            @RequestParam(value = "holdMs", defaultValue = "30") long holdMs,
            @RequestParam(value = "thinkMs", defaultValue = "100") long thinkMs,
            @RequestParam(value = "sql", defaultValue = "SELECT 1") String sql) {
        int n = Math.max(1, Math.min(threads, 500));
        long duration = Math.max(100L, Math.min(durationMs, 60000L));
        long hold = Math.max(0L, Math.min(holdMs, 2000L));
        long think = Math.max(0L, Math.min(thinkMs, 5000L));
        long deadline = System.currentTimeMillis() + duration;

        // 快照限流器累计拒绝计数，跑完后取差值得到本次压测的秒拒数（在途超限）
        long deniedQueueBase = connectionAdmissionLimiter.getDeniedQueueTotal();

        AtomicInteger ok = new AtomicInteger();
        AtomicInteger denied = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();
        AtomicInteger held = new AtomicInteger();
        AtomicInteger peakHeld = new AtomicInteger();
        AtomicLong latencyMaxMs = new AtomicLong();
        LongAdder latencySumMs = new LongAdder();
        AtomicLong requestCount = new AtomicLong();
        List<String> samples = Collections.synchronizedList(new ArrayList<>());

        long startAt = System.currentTimeMillis();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < n; i++) {
                executor.submit(() ->
                        runOneLoop(deadline, sql, hold, think, ok, denied, error, held, peakHeld,
                                latencySumMs, latencyMaxMs, requestCount, samples));
            }
            // executor.close() 有序关闭并阻塞至全部任务结束
        }
        long elapsed = System.currentTimeMillis() - startAt;
        long requests = requestCount.get();
        long totalOps = (long) ok.get() + denied.get() + error.get();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("threads", n);
        result.put("durationMs", duration);
        result.put("holdMs", hold);
        result.put("thinkMs", think);
        result.put("elapsedMs", elapsed);
        result.put("totalRequests", totalOps);
        result.put("successOps", ok.get());
        result.put("deniedByLimiter", denied.get());
        result.put("deniedByQueue", connectionAdmissionLimiter.getDeniedQueueTotal() - deniedQueueBase);
        result.put("maxWaiters", connectionAdmissionLimiter.getMaxWaiters());
        result.put("sqlErrors", error.get());
        result.put("denyRate", totalOps > 0 ? String.format("%.1f%%", denied.get() * 100.0 / totalOps) : "0%");
        result.put("peakHeldConnections", peakHeld.get());
        result.put("avgLatencyMs", requests > 0 ? latencySumMs.sum() / requests : 0);
        result.put("maxLatencyMs", latencyMaxMs.get());
        result.put("successPerSec", elapsed > 0 ? ok.get() * 1000L / elapsed : 0);
        result.put("errorSamples", samples);
        log.info("[db-stress] 压测完成: {}", result);
        return AjaxUtil.getInstance().success(result);
    }

    /**
     * 单会话压测循环：在截止时间前模拟真实业务——借连接、持有期间耗时、归还，再留出思考时间。
     */
    private void runOneLoop(long deadline, String sql, long holdMs, long thinkMs,
                            AtomicInteger ok, AtomicInteger denied, AtomicInteger error,
                            AtomicInteger held, AtomicInteger peakHeld,
                            LongAdder latencySumMs, AtomicLong latencyMaxMs, AtomicLong requestCount,
                            List<String> samples) {
        try {
            while (System.currentTimeMillis() < deadline) {
                long reqStart = System.nanoTime();
                try {
                    try (Connection conn = dataSource.getConnection();
                         Statement st = conn.createStatement();
                         ResultSet rs = st.executeQuery(sql)) {
                        int h = held.incrementAndGet();
                        peakHeld.accumulateAndGet(h, Math::max);
                        try {
                            rs.next();
                            if (holdMs > 0) {
                                Thread.sleep(holdMs);
                            }
                            ok.incrementAndGet();
                        } finally {
                            held.decrementAndGet();
                        }
                    }
                } catch (FormattedRuntimeException e) {
                    // 连接准入限流拒绝（排队超限 / 等待超时 / 中断）
                    denied.incrementAndGet();
                    addSample(samples, "DENIED: " + e.getMessage());
                } catch (SQLException e) {
                    error.incrementAndGet();
                    addSample(samples, "SQL: " + e.getMessage());
                }
                long latMs = (System.nanoTime() - reqStart) / 1_000_000L;
                latencySumMs.add(latMs);
                latencyMaxMs.accumulateAndGet(latMs, Math::max);
                requestCount.incrementAndGet();
                if (thinkMs > 0) {
                    Thread.sleep(thinkMs);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void addSample(List<String> samples, String msg) {
        if (samples.size() < 5) {
            samples.add(msg);
        }
    }

    @Override
    public String getBusinessName() {
        return "数据库连接压测";
    }
}
