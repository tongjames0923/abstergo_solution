package com.tbs.app.controller.privates;

import com.tbs.app.form.PushNotifyMessage;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.push.api.transport.IPushEndpoint;
import com.tbs.push.model.PushTopic;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 推送发布（私有）控制器
 * <p>
 * 置于 /private/** 由项目认证体系统一保护（需登录鉴权），面向系统通知发布场景：
 * 向指定 topic 发布一条系统通知，由默认 topic 直配协调器投递给
 * 全部订阅了该 topic 的在线会话（全局广播语义）。
 * 载荷为 {@link PushNotifyMessage} 强约束契约：契约校验在发布入口前置执行，非法载荷直接拒绝，
 * 超出契约的发布需求由后续开发者自行扩展新端点。
 * 原 /public/push/send 公开测试入口已收敛至此私有接口，发送能力不再对外匿名开放。
 * </p>
 * <p>
 * 使用示例（完整链路）：
 * <ol>
 *     <li>POST /private/push/auth → 获取 ticket</li>
 *     <li>GET /private/push/sse/connect?push-ticket=xxx → 建立SSE流，收到hello帧（含sessionId）</li>
 *     <li>POST /private/push/sse/upload → 提交结构化SUBSCRIBE帧订阅目标topic</li>
 *     <li>POST /private/push/send → 订阅该topic的会话收到data帧</li>
 * </ol>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/push")
@Tag(name = "推送发布控制器", description = "私有访问（需登录鉴权）的系统通知发布接口：发送与在线会话查询")
public class PrivatePushController extends BaseController {

    /**
     * 默认广播主题
     * <p>
     * 未显式指定topic时使用的全局主题（非空、长度≤128，符合PushTopic值对象约束），
     * 客户端订阅该topic即可接收全局广播。
     * </p>
     */
    public static final PushTopic GLOBAL_TOPIC = PushTopic.of("global.broadcast");

    /**
     * 推送统一门面
     */
    @Resource
    private IPushEndpoint pushEndpoint;

    /**
     * 发送消息表单
     * <p>
     * POST /private/push/send 的请求体，参数全部类型化：
     * <ul>
     *     <li>topic — {@link PushTopic}值对象，可选：非空且长度不超过128字符；
     *     缺省时使用全局广播主题 {@link #GLOBAL_TOPIC}</li>
     *     <li>payload — {@link PushNotifyMessage}强约束契约载荷，必填：id 与 type 必填，
     *     type/priority 白名单校验，timestamp/priority 缺省补齐，非法载荷发布时直接拒绝</li>
     * </ul>
     * 请求示例：{"topic": "global.system.notify.zhangsan", "payload": {"id": "M1705123456789",
     * "type": "notify", "title": "系统通知", "content": "您有一条新的审批待处理。", "priority": "normal"}}
     * </p>
     */
    public static class SendForm implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 业务主题，可选（非空且长度≤128字符），缺省为全局广播主题
         */
        private PushTopic topic;

        /**
         * 系统通知载荷（强约束契约），必填
         */
        private PushNotifyMessage payload;

        /**
         * 获取业务主题
         *
         * @return 业务主题值对象，缺省时为null
         */
        public PushTopic getTopic() {
            return topic;
        }

        /**
         * 设置业务主题
         *
         * @param topic 业务主题值对象
         */
        public void setTopic(PushTopic topic) {
            this.topic = topic;
        }

        /**
         * 获取系统通知载荷
         *
         * @return 强约束契约载荷对象
         */
        public PushNotifyMessage getPayload() {
            return payload;
        }

        /**
         * 设置系统通知载荷
         *
         * @param payload 强约束契约载荷对象
         */
        public void setPayload(PushNotifyMessage payload) {
            this.payload = payload;
        }
    }

    /**
     * 发送系统通知消息
     * <p>
     * 将系统通知载荷发布到指定topic（缺省为全局广播主题global.broadcast），
     * 消息进入消息源后由分发器投递给全部订阅了该topic的在线会话。
     * 载荷为{@link PushNotifyMessage}强约束契约：id/type必填、type/priority白名单校验、
     * timestamp/priority缺省补齐，非法载荷直接拒绝发布（i18n：push.notify.*）。
     * 接口位于 /private/**，须携带有效登录令牌（Authorization 头），匿名请求将被认证网关拦截。
     * </p>
     * <p>
     * 请求示例：{"topic": "global.system.notify.zhangsan", "payload": {"id": "M1705123456789",
     * "type": "notify", "title": "系统通知", "content": "您有一条新的审批待处理。", "priority": "normal"}}。
     * </p>
     *
     * @param form 发送表单（topic可选，payload为强约束契约载荷）
     * @return 统一响应（data：messageId消息唯一标识、topic实际投递主题、onlineCount投递时在线会话数）
     */
    @PostMapping("/send")
    @Operation(summary = "发送系统通知消息",
        description = "向指定topic（缺省global.broadcast）发布一条系统通知；payload为强约束契约（id/type必填，type/priority白名单校验），非法载荷直接拒绝；须登录鉴权")
    @ApiResponse(responseCode = "200", description = "发送成功，data含messageId/topic/onlineCount")
    @AjaxResponseType(Object.class)
    public AjaxResult send(@RequestBody SendForm form) {
        PushTopic topic = form.getTopic() != null ? form.getTopic() : GLOBAL_TOPIC;
        PushNotifyMessage message = form.getPayload();
        AssertUtil.isTrue(message == null).i18n(PushNotifyMessage.I18N_PAYLOAD_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        message.prepare();
        String messageId = pushEndpoint.publish(topic.getValue(), message);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("messageId", messageId);
        data.put("topic", topic.getValue());
        data.put("onlineCount", pushEndpoint.onlineCount());
        return successResult(data);
    }

    /**
     * 查询当前在线会话数量
     * <p>
     * 返回推送服务当前登记的全部在线会话数量（SSE与WebSocket会话统一计数），
     * 可用于发布前确认有会话在线。接口位于 /private/**，须登录鉴权。
     * </p>
     * <p>
     * 请求示例：GET /private/push/online（无参数）。
     * </p>
     *
     * @return 统一响应（data：onlineCount当前在线会话数量）
     */
    @GetMapping("/online")
    @Operation(summary = "查询在线会话数量", description = "返回推送服务当前登记的全部在线会话数量；须登录鉴权")
    @ApiResponse(responseCode = "200", description = "查询成功，data含onlineCount")
    @AjaxResponseType(Object.class)
    public AjaxResult online() {
        return successResult(Map.of("onlineCount", pushEndpoint.onlineCount()));
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "推送发布";
    }

}
