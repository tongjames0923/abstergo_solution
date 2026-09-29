using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using TbsSdk.Core;
using TbsSdk.Models;

namespace TbsSdk.Push
{
    /// <summary>
    /// 推送消息中心接口封装（对齐后端 PrivatePushMessageController，基础路径 /private/push/message）。
    /// <para>新链路（信令+拉取）：SSE 仅承载协议帧，业务消息内容一律通过
    /// <see cref="FetchMessagesAsync"/> 拉取，由 SIGNAL 信令触发。</para>
    /// </summary>
    public sealed class PushMessageClient
    {
        private readonly TbsHttpClient _http;

        public PushMessageClient(TbsHttpClient http)
        {
            _http = http ?? throw new System.ArgumentNullException(nameof(http));
        }

        /// <summary>
        /// 分页拉取当前用户消息列表（到达序倒序，含已读）。
        /// </summary>
        /// <param name="cursor">游标：上一页响应返回的 cursor（接收行 id），首页不传。</param>
        /// <param name="limit">每页条数，默认 20，上限 100。</param>
        /// <param name="readFlag">读取范围：all（全部）/ unread（仅未读），缺省为全部。</param>
        public async Task<PushMessagePageVO> FetchMessagesAsync(long? cursor = null, int? limit = null,
            string readFlag = null, CancellationToken cancellationToken = default)
        {
            var query = new Dictionary<string, object>();
            if (cursor.HasValue)
            {
                query["cursor"] = cursor.Value;
            }
            if (limit.HasValue)
            {
                query["limit"] = limit.Value;
            }
            if (!string.IsNullOrEmpty(readFlag))
            {
                query["readFlag"] = readFlag;
            }

            var result = await _http.GetAsync(PushConstants.MessageListPath, query, null, cancellationToken)
                .ConfigureAwait(false);
            return result.GetData<PushMessagePageVO>() ?? new PushMessagePageVO();
        }

        /// <summary>
        /// 查询当前用户未读数（业务体为裸 Long 数字）。
        /// </summary>
        public async Task<long> FetchUnreadCountAsync(CancellationToken cancellationToken = default)
        {
            var result = await _http.GetAsync(PushConstants.MessageUnreadCountPath, null, null, cancellationToken)
                .ConfigureAwait(false);
            var count = result.GetData<long?>();
            return count ?? 0;
        }

        /// <summary>
        /// 批量记录已读（写 READ 操作日志）。请求体为裸 Long 数组（非包装对象）。
        /// </summary>
        /// <returns>实际记录条数。</returns>
        public async Task<int> MarkReadAsync(IEnumerable<long> messageIds,
            CancellationToken cancellationToken = default)
        {
            if (messageIds == null)
            {
                throw new System.ArgumentNullException(nameof(messageIds));
            }

            var result = await _http.PostAsync(PushConstants.MessageReadPath, messageIds,
                null, null, cancellationToken).ConfigureAwait(false);
            return result.GetData<int?>() ?? 0;
        }

        /// <summary>
        /// 通用操作回传（CLICK 等，预留扩展）。
        /// </summary>
        /// <returns>实际写入日志条数。</returns>
        public async Task<int> OperateMessageAsync(long messageId, string opType, string extInfo = null,
            CancellationToken cancellationToken = default)
        {
            var result = await _http.PostAsync(PushConstants.MessageOperatePath,
                new PushMessageOperateForm { MessageId = messageId, OpType = opType, ExtInfo = extInfo },
                null, null, cancellationToken).ConfigureAwait(false);
            return result.GetData<int?>() ?? 0;
        }

        /// <summary>
        /// 发送持久化通知（新链路发送入口，非旧全量推送）。
        /// <para>注意：接收人不随表单传递，由后端 INotifyReceiverResolver SPI 解析（默认实现为空集=无人接收）。</para>
        /// </summary>
        /// <returns>发送结果视图（receiverCount 为真实送达规模，0 为合法状态）。</returns>
        public async Task<PushSendResultVO> SendNotifyAsync(PushNotifySendForm form,
            CancellationToken cancellationToken = default)
        {
            if (form == null)
            {
                throw new System.ArgumentNullException(nameof(form));
            }
            if (string.IsNullOrEmpty(form.MsgType))
            {
                throw new System.ArgumentException("msgType 必填", nameof(form));
            }

            var result = await _http.PostAsync(PushConstants.MessageNotifyPath, form,
                null, null, cancellationToken).ConfigureAwait(false);
            return result.GetData<PushSendResultVO>();
        }
    }
}
