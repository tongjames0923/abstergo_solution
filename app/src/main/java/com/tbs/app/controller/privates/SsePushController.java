package com.tbs.app.controller.privates;

import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.model.PushTicket;
import com.tbs.push.sse.adapter.SseTransportAdapter;
import com.tbs.app.form.SseUploadForm;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.annotation.NoWrapped;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 推送SSE通道控制器
 * <p>
 * SSE组合通道的HTTP接入点：
 * <ul>
 *     <li>GET connect — 建立SSE下行流（@NoWrapped跳过统一响应包装，直接返回SseEmitter）</li>
 *     <li>POST upload — 客户端→服务端上行端点，接收结构化协议帧，返回统一{@link AjaxResult}</li>
 * </ul>
 * 端点置于/private/**由项目认证体系统一保护；本控制器不集成任何鉴权逻辑，
 * 与协议层"默认不集成安全验证"的定位一致。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/push/sse")
@Tag(name = "推送SSE通道控制器", description = "推送SSE组合通道：SseEmitter下行流 + HTTP POST结构化帧上行")
public class SsePushController extends BaseController {

    /**
     * SSE传输适配器
     */
    private final SseTransportAdapter adapter;

    /**
     * 构造SSE通道控制器
     *
     * @param adapter       SSE传输适配器
     * @param objectMapper  JSON解析器
     */
    public SsePushController(SseTransportAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * 建立SSE下行连接
     * <p>
     * 统一连接建立流程：携认证端点签发的一次性连接凭证（push-ticket）建连，凭证经
     * {@link PushTicket}值对象校验非空、由协议层原子消费（防重放）后，以预分配sessionId
     * 建立会话并下发握手确认帧（SSE事件名hello，载荷含sessionId与协议版本）。
     * </p>
     * <p>
     * 使用示例：GET /private/push/sse/connect?push-ticket=abcd1234...（EventSource连接）；
     * 凭证缺失/无效/过期时返回统一错误响应（i18n：push.ticket.required / push.ticket.invalid /
     * push.ticket.expired）。
     * </p>
     *
     * @param ticket 一次性连接凭证（必填，认证端点POST /private/push/auth签发，
     *               TTL默认60秒，query param携带）
     * @return SSE下行流emitter（事件流，非统一响应结构）
     */
    @NoWrapped
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "建立SSE下行连接",
        description = "携一次性凭证建连，校验通过后下发hello握手确认帧（载荷含sessionId与协议版本）")
    @ApiResponse(responseCode = "200", description = "SSE事件流建立成功，首帧为hello握手确认；凭证无效时返回统一错误响应")
    public SseEmitter connect(
        @Parameter(name = "push-ticket", required = true, description = "认证端点签发的一次性连接凭证") @RequestParam(
            PushFrameConstants.TICKET_PARAM) String ticket) {
        PushTicket validatedTicket = PushTicket.of(ticket);
        return adapter.connect(validatedTicket.getValue());
    }

    /**
     * 上行结构化协议帧
     * <p>
     * 客户端携sessionId上行协议帧（HELLO/PING/SUBSCRIBE/UNSUBSCRIBE/DATA/ERROR），
     * 帧以结构化对象传输：type为帧类型枚举（必填），version为协议版本（当前为1），
     * topic为订阅主题值对象（SUBSCRIBE/UNSUBSCRIBE/DATA帧必填，非空且≤128字符），
     * payload为帧载荷文本。会话无效或帧缺失时快速失败并返回统一错误响应。
     * </p>
     * <p>
     * 请求示例（订阅全局广播主题）：
     * {"sessionId":"<hello帧下发的会话标识>","frame":{"version":1,"type":"SUBSCRIBE","topic":"global.broadcast"}}
     * </p>
     *
     * @param form 上行请求表单（sessionId必填非空；frame必填，type必填，topic按帧类型要求）
     * @return 统一响应（成功时data为空；帧处理产生的确认/错误帧经SSE下行流异步下发）
     */
    @PostMapping("/upload")
    @Operation(summary = "上行结构化协议帧",
        description = "客户端携sessionId上行结构化协议帧（HELLO/PING/SUBSCRIBE/UNSUBSCRIBE/DATA等），服务端按帧类型统一处理")
    @ApiResponse(responseCode = "200", description = "上行受理成功；会话不存在/未就绪时返回统一错误响应")
    @AjaxResponseType(Object.class)
    public AjaxResult upload(@RequestBody SseUploadForm form) {
        adapter.uploadFrame(form.getSessionId().getValue(), form.getFrame());
        return successResult();
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "推送SSE通道";
    }

}
