package com.tbs.push.api.coordinator;

import com.tbs.push.api.session.IConnectionSession;
import com.tbs.push.model.message.PushMessage;

import java.util.List;

/**
 * 投递协调器SPI
 * <p>
 * "投递给谁"的唯一决策点：分发器取出消息后交由协调器解析投递目标会话集合。
 * 默认实现按topic匹配订阅会话（TopicRouteCoordinator）；业务可注册自定义实现，
 * 依据消息元数据与会话属性实现定向投递（如按用户ID、租户等维度路由）。
 * 返回的会话列表允许包含非CONNECTED状态的会话，由分发器投递时按发送结果如实记录。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IRouteCoordinator {

    /**
     * 解析投递目标会话集合
     *
     * @param message 待投递的业务消息
     * @return 投递目标会话列表，不为null（可为空列表，表示无目标）
     */
    List<IConnectionSession> resolveTargets(PushMessage message);

}
