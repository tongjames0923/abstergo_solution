package com.tbs.app.service.push;

import com.tbs.app.form.PushNotifySendForm;

import java.util.List;

/**
 * 推送通知接收人解析 SPI
 * <p>
 * "消息应该让谁收到"的唯一决策点：实现方按消息特征返回接收用户标识列表， 解析结果直接决定 fan-out 落库范围与信令发布范围。 抛出异常视为发送失败（事务整体回滚），返回空列表视为合法的"无接收人"（纯落库审计语义）。
 * 可注册多个实现，发送时按 supports 命中链式分派；默认实现返回空集， 业务方注册实现 Bean 即覆盖。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface INotifyReceiverResolver {

    /**
     * 是否支持解析该消息（多实现按命中分派）
     *
     * @param form 发送表单
     * @return true表示由本实现解析
     */
    boolean supports(PushNotifySendForm form);

    /**
     * 解析接收人列表（Service 侧兜底去重）
     *
     * @param form 发送表单
     * @return 接收用户标识列表，可为空列表
     */
    List<Long> resolveReceivers(PushNotifySendForm form);

}
