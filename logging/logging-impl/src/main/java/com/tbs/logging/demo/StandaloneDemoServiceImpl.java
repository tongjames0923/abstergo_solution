package com.tbs.logging.demo;

import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 独立泛化调用演示服务实现
 * <p>
 * 只做无副作用的换算与文本加工，结果同时打日志，便于从服务端确认「泛化调用真的进来了、参数还原成了什么」。
 * 所有入参都按可能为 {@code null} 处理：泛化调用不经过编译期检查，缺参、传空都是常态而非异常。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class StandaloneDemoServiceImpl implements IStandaloneDemoService {

    /**
     * 回显重复次数上限，防止演示接口被拉满
     */
    private static final int MAX_REPEAT = 10;

    /**
     * 金额默认小数位
     */
    private static final int DEFAULT_SCALE = 2;

    @Override
    public String ping() {
        log.info("[StandaloneDemo] ping 调用到达");
        return "pong";
    }

    @Override
    public String echo(String message) {
        log.info("[StandaloneDemo] echo(单参) message={}", message);
        return "echo:" + StrUtil.nullToEmpty(message);
    }

    @Override
    public String echo(String message, Integer times) {
        int repeat = times == null || times <= 0 ? 1 : Math.min(times, MAX_REPEAT);
        log.info("[StandaloneDemo] echo(两参) message={}, times={}, 实际重复={}", message, times, repeat);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            builder.append(StrUtil.nullToEmpty(message));
        }
        return builder.toString();
    }

    @Override
    public long sum(long a, long b) {
        log.info("[StandaloneDemo] sum a={}, b={}", a, b);
        return a + b;
    }

    @Override
    public StandaloneDemoVo transform(StandaloneDemoVo payload) {
        if (payload == null) {
            log.info("[StandaloneDemo] transform 入参为空");
            return null;
        }
        log.info("[StandaloneDemo] transform 收到载荷 {} / {} / {} / {} / {}", payload.getCode(), payload.getName(),
            payload.getQty(), payload.getAmount(), payload.getTags());
        StandaloneDemoVo transformed = new StandaloneDemoVo();
        transformed.setCode(payload.getCode());
        transformed.setName(payload.getName() == null ? null : payload.getName().toUpperCase());
        transformed.setQty(payload.getQty() == null ? 1 : payload.getQty() + 1);
        transformed.setAmount(payload.getAmount() == null ? BigDecimal.ZERO : payload.getAmount().multiply(BigDecimal.TWO));
        transformed.setTags(CollUtil.isEmpty(payload.getTags()) ? new ArrayList<>() : new ArrayList<>(payload.getTags()));
        return transformed;
    }

    @Override
    public List<StandaloneDemoVo> filter(List<StandaloneDemoVo> payloads, String tag) {
        log.info("[StandaloneDemo] filter 入参条数={}, tag={}", payloads == null ? 0 : payloads.size(), tag);
        List<StandaloneDemoVo> matched = new ArrayList<>();
        if (CollUtil.isEmpty(payloads)) {
            return matched;
        }
        for (StandaloneDemoVo payload : payloads) {
            if (payload == null) {
                continue;
            }
            if (StrUtil.isBlank(tag) || (payload.getTags() != null && payload.getTags().contains(tag))) {
                matched.add(payload);
            }
        }
        return matched;
    }

    @Override
    public String status(StandaloneDemoStatus status) {
        log.info("[StandaloneDemo] status 还原结果={}", status);
        return status == null ? "NULL" : status.name() + "/" + status.getLabel();
    }

    @Override
    public BigDecimal round(BigDecimal amount, Integer scale) {
        int digits = scale == null || scale < 0 ? DEFAULT_SCALE : scale;
        log.info("[StandaloneDemo] round amount={}, scale={}, 实际位数={}", amount, scale, digits);
        if (amount == null) {
            return BigDecimal.ZERO.setScale(digits, RoundingMode.HALF_UP);
        }
        return amount.setScale(digits, RoundingMode.HALF_UP);
    }
}
