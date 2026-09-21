package com.tbs.app.controller.privates;

import com.tbs.app.form.PushMessageOperateForm;
import com.tbs.app.form.PushMessageQueryForm;
import com.tbs.app.form.PushNotifySendForm;
import com.tbs.app.service.push.IPushNotifyService;
import com.tbs.app.vo.PushMessageChainVO;
import com.tbs.app.vo.PushMessagePageVO;
import com.tbs.knife4j.annotation.AjaxResponseType;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 推送消息中心（私有）控制器
 * <p>
 * 薄壳：仅做 HTTP 接入、参数组装与统一响应包装，全部业务逻辑委托给 {@link IPushNotifyService}（契约在 system-api，实现在 default-system-impl）。 所有接口位于
 * /private/**，须携带有效登录令牌，由项目认证体系统一鉴权。 信令+拉取链路的新入口；旧 {@code /private/push/send} 全量推送接口保持不变。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@RestController
@RequestMapping("/private/push/message")
@Tag(name = "推送消息中心控制器",
    description = "私有访问（需登录鉴权）的消息中心接口：发送、拉取、未读、已读、操作回传、全链路")
public class PrivatePushMessageController extends BaseController {

    /**
     * 推送消息中心服务（system 体系实现）
     */
    @Resource
    private IPushNotifyService pushNotifyService;

    /**
     * 发送持久化通知
     * <p>
     * 落库（主表+fan-out+SEND日志）与事务提交后发布 SIGNAL 信令均由服务内部完成； 返回生成的消息ID（Long）。
     * </p>
     *
     * @param form 发送表单（msgType 必填，其余可选）
     * @return 统一响应（data：消息ID）
     */
    @PostMapping("/notify")
    @Operation(summary = "发送持久化通知",
        description = "提交消息内容并落库，事务提交后向接收人发布信令；msgType必填，其余字段可选")
    @ApiResponse(responseCode = "200", description = "发送成功，data为消息ID")
    @AjaxResponseType(Long.class)
    public AjaxResult notify(@RequestBody PushNotifySendForm form) {
        return successResult(pushNotifyService.send(form));
    }

    /**
     * 分页拉取当前用户消息列表
     * <p>
     * 到达序倒序、含已读；cursor 为上一页响应的游标，readFlag=unread 时仅筛选未读。
     * </p>
     *
     * @param query 查询表单（cursor/limit/readFlag）
     * @return 统一响应（data：分页结果）
     */
    @GetMapping("/list")
    @Operation(summary = "分页拉取消息列表",
        description = "当前用户消息列表，到达序倒序、含已读；cursor翻页，readFlag=unread时仅未读")
    @ApiResponse(responseCode = "200", description = "查询成功，data为分页结果")
    @AjaxResponseType(PushMessagePageVO.class)
    public AjaxResult list(PushMessageQueryForm query) {
        return successResult(pushNotifyService.fetchMessages(query));
    }

    /**
     * 统计当前用户未读数
     *
     * @return 统一响应（data：未读数）
     */
    @GetMapping("/unread-count")
    @Operation(summary = "统计未读数", description = "派生当前用户未读消息数（接收关系数 − 已读消息DISTINCT数）")
    @ApiResponse(responseCode = "200", description = "查询成功，data为未读数")
    @AjaxResponseType(Long.class)
    public AjaxResult unreadCount() {
        return successResult(pushNotifyService.unreadCount());
    }

    /**
     * 批量标记已读
     *
     * @param messageIds 消息ID列表（非空，单次≤100）
     * @return 统一响应（data：实际记录的已读条数）
     */
    @PostMapping("/read")
    @Operation(summary = "批量标记已读",
        description = "对当前用户的接收关系批量写入READ日志，幂等；非当前用户消息静默跳过")
    @ApiResponse(responseCode = "200", description = "回传成功，data为实际记录条数")
    @AjaxResponseType(Integer.class)
    public AjaxResult read(@RequestBody List<Long> messageIds) {
        return successResult(pushNotifyService.markRead(messageIds));
    }

    /**
     * 通用操作回传（CLICK 等）
     *
     * @param form 操作回传表单（messageId 与 opType 必填）
     * @return 统一响应（data：实际写入的日志条数）
     */
    @PostMapping("/operate")
    @Operation(summary = "操作回传", description = "用户对消息的处理行为回传（CLICK等）；CLICK默认补记一条READ")
    @ApiResponse(responseCode = "200", description = "回传成功，data为实际写入日志条数")
    @AjaxResponseType(Integer.class)
    public AjaxResult operate(@RequestBody PushMessageOperateForm form) {
        return successResult(pushNotifyService.recordOperation(form));
    }

    /**
     * 查询单消息全链路
     * <p>
     * 面向管理端审计：SEND行为 + 投递关系 + 处理行为拼图。
     * </p>
     *
     * @param messageId 消息ID
     * @return 统一响应（data：全链路视图）
     */
    @GetMapping("/chain")
    @Operation(summary = "查询消息全链路", description = "单消息审计视图：SEND事件 + 投递关系 + 处理事件")
    @ApiResponse(responseCode = "200", description = "查询成功，data为全链路视图")
    @AjaxResponseType(PushMessageChainVO.class)
    public AjaxResult chain(@RequestParam("messageId") Long messageId) {
        return successResult(pushNotifyService.queryChain(messageId));
    }

    /**
     * 获取当前控制器的业务名称
     *
     * @return 业务名称
     */
    @Override
    public String getBusinessName() {
        return "推送消息中心";
    }

}
