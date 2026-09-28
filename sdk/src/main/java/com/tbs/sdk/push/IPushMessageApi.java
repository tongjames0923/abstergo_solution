package com.tbs.sdk.push;

import com.tbs.sdk.model.PushMessageChain;
import com.tbs.sdk.model.PushMessageOperateForm;
import com.tbs.sdk.model.PushMessagePage;
import com.tbs.sdk.model.PushMessageQueryForm;
import com.tbs.sdk.model.PushNotifySendForm;
import com.tbs.sdk.model.PushSendResult;
import java.util.List;

/**
 * 推送消息中心契约
 * <p>
 * 对接服务端/private/push/message/*全量接口（信令+拉取新链路），
 * 全部接口需登录令牌，由HTTP层自动注入。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushMessageApi {

    /**
     * 发送持久化通知
     * <p>
     * 服务端完成落库（主表+fan-out+SEND日志）与事务提交后信令发布，
     * 调用方以receiverCount为准构建真实送达回执。
     * </p>
     *
     * @param form 发送表单（msgType必填，其余可选）
     * @return 发送结果（messageId/topic/msgType/receiverCount/signalCount）
     * @throws com.tbs.sdk.exception.SdkAuthException   未登录或令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端校验失败或错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    PushSendResult notify(PushNotifySendForm form);

    /**
     * 游标分页拉取当前用户消息列表
     * <p>
     * 到达序倒序、含已读；cursor取上一页响应的游标，readFlag=unread时仅未读。
     * </p>
     *
     * @param query 查询表单（cursor/limit/readFlag均可为null走缺省）
     * @return 分页结果（cursor为下一页游标，无更多为null）
     * @throws com.tbs.sdk.exception.SdkAuthException   未登录或令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    PushMessagePage list(PushMessageQueryForm query);

    /**
     * 统计当前用户未读数
     *
     * @return 未读消息数
     * @throws com.tbs.sdk.exception.SdkAuthException   未登录或令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    Long unreadCount();

    /**
     * 批量标记已读
     * <p>
     * 幂等；非当前用户消息服务端静默跳过。
     * </p>
     *
     * @param messageIds 消息ID列表（非空，单次≤100）
     * @return 实际记录的已读条数
     * @throws com.tbs.sdk.exception.SdkAuthException   未登录或令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端校验失败或错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    Integer markRead(List<Long> messageIds);

    /**
     * 通用操作回传（CLICK等）
     *
     * @param form 操作回传表单（messageId与opType必填）
     * @return 实际写入的日志条数
     * @throws com.tbs.sdk.exception.SdkAuthException   未登录或令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端校验失败或错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    Integer operate(PushMessageOperateForm form);

    /**
     * 查询单消息全链路（SEND事件+投递关系+处理行为）
     *
     * @param messageId 消息ID
     * @return 全链路审计视图
     * @throws com.tbs.sdk.exception.SdkAuthException   未登录或令牌失效时抛出
     * @throws com.tbs.sdk.exception.SdkServerException 服务端错误时抛出
     * @throws com.tbs.sdk.exception.SdkClientException 网络失败或响应非法时抛出
     */
    PushMessageChain chain(Long messageId);
}
