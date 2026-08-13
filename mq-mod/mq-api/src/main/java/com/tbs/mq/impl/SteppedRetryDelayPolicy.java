package com.tbs.mq.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.api.retry.IRetryDelayPolicy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 档位式重试延迟策略
 * <p>
 * 按预设档位数组返回延迟时长：第N次重试取第N档（从0计），
 * 超出档位数量后恒定取最后一档。
 * 默认档位为30秒、2分钟、10分钟，可通过构造器自定义。
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
public class SteppedRetryDelayPolicy implements IRetryDelayPolicy {

    /**
     * 默认延迟档位：30秒、2分钟、10分钟
     */
    private static final List<Duration> DEFAULT_STEPS = Arrays.asList(
        Duration.ofSeconds(30),
        Duration.ofMinutes(2),
        Duration.ofMinutes(10)
    );

    /**
     * 延迟档位列表
     */
    private final List<Duration> steps;

    /**
     * 构造默认档位策略
     */
    public SteppedRetryDelayPolicy() {
        this(DEFAULT_STEPS);
    }

    /**
     * 构造自定义档位策略
     *
     * @param steps 延迟档位列表，不能为空
     */
    public SteppedRetryDelayPolicy(List<Duration> steps) {
        AssertUtil.isTrue(CollUtil.isEmpty(steps))
            .i18n("mq.retry.delay.steps.required")
            .throwIt();
        this.steps = new ArrayList<>(steps);
    }

    /**
     * 计算下一次重投递的延迟时长
     *
     * @param retryCount 当前已完成的重试次数
     * @return 对应档位的延迟时长，超出档位后取最后一档
     */
    @Override
    public Duration nextDelay(int retryCount) {
        int index = Math.max(0, Math.min(retryCount, steps.size() - 1));
        return steps.get(index);
    }

}
