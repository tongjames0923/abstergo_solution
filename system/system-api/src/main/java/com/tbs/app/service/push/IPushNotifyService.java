package com.tbs.app.service.push;

import com.tbs.app.form.PushMessageOperateForm;
import com.tbs.app.form.PushMessageQueryForm;
import com.tbs.app.form.PushNotifySendForm;
import com.tbs.app.vo.PushMessageChainVO;
import com.tbs.app.vo.PushMessagePageVO;
import com.tbs.app.vo.PushSendResultVO;

import java.util.Date;
import java.util.List;

/**
 * 推送消息中心服务
 * <p>
 * 新链路（SIGNAL 信令 + 拉取）的业务契约： 发送侧"主表+fan-out接收人+SEND日志"同事务原子落库，事务提交后逐接收人发布信令（信令失败不影响成功语义）；
 * 查询侧提供到达序倒序的游标分页列表、日志派生的未读数、批量已读（写 READ 日志）、 通用操作回传与单消息全链路查询。 已读/未读不落状态列，全部由操作日志派生。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IPushNotifyService {

    /**
     * 发送持久化通知
     * <p>
     * 契约校验 → 主表落库 → SPI 解析接收人（去重）→ fan-out 落库 → SEND 日志落库， 同一事务原子完成；事务提交后逐接收人发布 SIGNAL 信令（推送未启用或信令失败仅记日志）。
     * 接收人列表为空是合法状态（消息仅落库，无信令）。 返回结果回传消息ID、实际主题、接收人数与信令数，供调用方构建真实送达回执。
     * </p>
     *
     * @param form 发送表单
     * @return 发送结果视图（messageId/topic/msgType/receiverCount/signalCount）
     */
    PushSendResultVO send(PushNotifySendForm form);

    /**
     * 分页拉取当前用户消息列表
     * <p>
     * 到达序倒序（游标=接收行ID）、含已读；readFlag=unread 时为列表筛选条件， 页内条数可能少于 limit。返回 cursor 供下一页查询。
     * </p>
     *
     * @param form 查询表单（cursor/limit/readFlag）
     * @return 分页结果
     */
    PushMessagePageVO fetchMessages(PushMessageQueryForm form);

    /**
     * 统计当前用户未读数
     * <p>
     * 派生算法：接收关系行数 − 已读消息 DISTINCT 数（重复 READ 日志不影响结果）。
     * </p>
     *
     * @return 未读数
     */
    long unreadCount();

    /**
     * 批量记录已读
     * <p>
     * 对属于当前用户的接收关系逐条写入 READ 操作日志； 不属于当前用户或不存在的消息ID静默跳过；已存在 READ 记录的消息跳过（幂等）。
     * </p>
     *
     * @param messageIds 消息ID列表（非空，单次≤100）
     * @return 实际记录的已读条数
     */
    int markRead(List<Long> messageIds);

    /**
     * 通用操作回传（CLICK 等扩展操作）
     * <p>
     * 校验接收关系后写入操作日志；CLICK 是否派生已读由业务规则决定 （当前默认：点击同时补记一条 READ）。
     * </p>
     *
     * @param form 操作回传表单
     * @return 实际写入的日志条数
     */
    int recordOperation(PushMessageOperateForm form);

    /**
     * 查询单消息全链路
     * <p>
     * 三源拼图：SEND 行为（日志表）+ 投递关系（接收人表）+ 处理行为（日志表）， 面向管理端审计。
     * </p>
     *
     * @param messageId 消息ID
     * @return 全链路视图
     */
    PushMessageChainVO queryChain(Long messageId);

    /**
     * 清理截止时间之前的消息数据（物理删除）
     * <p>
     * 供外部定时任务调用，按 create_time 分批删除三表数据，返回清理总行数。
     * </p>
     *
     * @param deadline 截止时间（严格早于该时间的数据被清理）
     * @return 清理总行数
     */
    int cleanupBefore(Date deadline);

}
