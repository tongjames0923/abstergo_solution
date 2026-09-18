package com.tbs.app.controller.privates;

import com.tbs.push.constants.PushFrameConstants;
import com.tbs.push.sse.adapter.SseTransportAdapter;
import com.tbs.push.sse.controller.SseUploadForm;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.annotation.NoWrapped;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 推送SSE通道控制器
 * <p>
 * SSE组合通道的HTTP接入点：
 * <ul>
 *     <li>GET connect — 建立SSE下行流（@NoWrapped跳过统一响应包装，直接返回SseEmitter）</li>
 *     <li>POST upload — 客户端→服务端上行端点，返回统一{@link AjaxResult}（总纲铁律8）</li>
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
public class SsePushController extends BaseController {

    /**
     * SSE传输适配器
     */
    private final SseTransportAdapter adapter;

    /**
     * 构造SSE通道控制器
     *
     * @param adapter SSE传输适配器
     */
    public SsePushController(SseTransportAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * 建立SSE下行连接
     * <p>
     * 统一连接建立流程：携认证端点签发的 push-ticket 建连，凭证校验通过后 以预分配sessionId建立会话并下发握手确认帧（事件名hello，载荷含sessionId与协议版本）。
     * </p>
     *
     * @param ticket 一次性连接凭证（认证端点签发，query param携带）
     * @return SSE下行流emitter
     */
    @NoWrapped
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam(PushFrameConstants.TICKET_PARAM) String ticket) {
        return adapter.connect(ticket);
    }

    /**
     * 上行协议帧
     * <p>
     * 客户端携sessionId上行帧文本（HELLO/PING/SUBSCRIBE/UNSUBSCRIBE/DATA等）， 会话无效时快速失败并返回统一错误响应。
     * </p>
     *
     * @param form 上行请求表单
     * @return 统一响应
     */
    @PostMapping("/upload")
    public AjaxResult upload(@RequestBody SseUploadForm form) {
        adapter.uploadText(form.getSessionId(), form.getFrame());
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
