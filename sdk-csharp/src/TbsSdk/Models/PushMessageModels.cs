using System;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;
using TbsSdk.Push;

namespace TbsSdk.Models
{
    /// <summary>
    /// 后端分页拉取返回的消息视图（对齐 PushNotifyMessageVO，与
    /// /private/push/message/list 返回体一一对应）。
    /// </summary>
    public sealed class PushMessageVO
    {
        /// <summary>消息 ID（接收行 id）。</summary>
        [JsonPropertyName("messageId")]
        public long? MessageId { get; set; }

        /// <summary>消息类型（notify/warning/reminder，业务可扩展）。</summary>
        [JsonPropertyName("msgType")]
        public string MsgType { get; set; }

        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("content")]
        public string Content { get; set; }

        /// <summary>优先级（low/normal/high/urgent）。</summary>
        [JsonPropertyName("priority")]
        public string Priority { get; set; }

        [JsonPropertyName("sender")]
        public string Sender { get; set; }

        /// <summary>跳转地址（可选）。</summary>
        [JsonPropertyName("url")]
        public string Url { get; set; }

        /// <summary>创建时间（毫秒时间戳或字符串日期均可解析）。</summary>
        [JsonPropertyName("createTime")]
        public DateTime? CreateTime { get; set; }

        /// <summary>已读标记。</summary>
        [JsonPropertyName("readFlag")]
        public bool? ReadFlag { get; set; }

        /// <summary>其余未映射字段。</summary>
        [JsonExtensionData]
        public IDictionary<string, JsonElement> Extra { get; set; }

        /// <summary>
        /// 规范化为客户端消息对象（对齐前端 normalizeFetchedMessage）。
        /// messageId 缺失时返回 null。
        /// </summary>
        public PushMessage Normalize()
        {
            if (MessageId == null)
            {
                return null;
            }
            return new PushMessage
            {
                Id = MessageId.Value.ToString(CultureInfo.InvariantCulture),
                Type = MsgType,
                Title = Title ?? string.Empty,
                Content = Content ?? string.Empty,
                Timestamp = CreateTime ?? DateTime.Now,
                Priority = PushMessagePriority.IsKnown(Priority) ? Priority : PushMessagePriority.Normal,
                Sender = Sender ?? string.Empty,
                Url = Url ?? string.Empty,
                Read = ReadFlag == true
            };
        }
    }

    /// <summary>
    /// 消息分页视图（对齐 PushMessagePageVO）：到达序倒序，游标分页。
    /// </summary>
    public sealed class PushMessagePageVO
    {
        /// <summary>消息列表。</summary>
        [JsonPropertyName("list")]
        public List<PushMessageVO> List { get; set; } = new List<PushMessageVO>();

        /// <summary>下一页游标（上一页响应返回的 cursor；无更多数据时为 null）。</summary>
        [JsonPropertyName("cursor")]
        public long? Cursor { get; set; }

        /// <summary>是否还有更多。</summary>
        [JsonPropertyName("hasMore")]
        public bool HasMore { get; set; }

        /// <summary>批量规范化为客户端消息列表（过滤无效项）。</summary>
        public List<PushMessage> Normalize()
        {
            if (List == null || List.Count == 0)
            {
                return new List<PushMessage>();
            }
            return List.Select(item => item.Normalize()).Where(item => item != null).ToList();
        }
    }

    /// <summary>
    /// 规范化后的客户端消息对象（对齐前端 normalizeFetchedMessage 的产出结构）。
    /// </summary>
    public sealed class PushMessage
    {
        /// <summary>消息标识（字符串化 messageId）。</summary>
        public string Id { get; set; }

        /// <summary>消息类型。</summary>
        public string Type { get; set; }

        public string Title { get; set; }

        public string Content { get; set; }

        /// <summary>到达时间。</summary>
        public DateTime Timestamp { get; set; }

        /// <summary>优先级（未知值归一为 normal）。</summary>
        public string Priority { get; set; }

        public string Sender { get; set; }

        public string Url { get; set; }

        /// <summary>是否已读。</summary>
        public bool Read { get; set; }
    }

    /// <summary>
    /// 发送持久化通知表单（对齐 PushNotifySendForm）。
    /// <para>接收人不随表单传递，由后端 INotifyReceiverResolver SPI 解析（默认实现为空集=无人接收）。</para>
    /// </summary>
    public sealed class PushNotifySendForm
    {
        /// <summary>消息类型（必填，如 notify/warning/reminder）。</summary>
        [JsonPropertyName("msgType")]
        public string MsgType { get; set; }

        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("content")]
        public string Content { get; set; }

        [JsonPropertyName("priority")]
        public string Priority { get; set; }

        [JsonPropertyName("url")]
        public string Url { get; set; }

        /// <summary>来源标识（可选）。</summary>
        [JsonPropertyName("source")]
        public string Source { get; set; }

        /// <summary>扩展信息（可选）。</summary>
        [JsonPropertyName("extInfo")]
        public string ExtInfo { get; set; }
    }

    /// <summary>
    /// 发送结果视图（对齐 PushSendResultVO）。
    /// <para>receiverCount 是真实送达规模（HTTP 成功 ≠ 送达），0 为合法状态（仅落库无信令）。</para>
    /// </summary>
    public sealed class PushSendResultVO
    {
        [JsonPropertyName("messageId")]
        public long? MessageId { get; set; }

        [JsonPropertyName("topic")]
        public string Topic { get; set; }

        [JsonPropertyName("msgType")]
        public string MsgType { get; set; }

        [JsonPropertyName("receiverCount")]
        public int? ReceiverCount { get; set; }

        [JsonPropertyName("signalCount")]
        public int? SignalCount { get; set; }
    }

    /// <summary>
    /// 通用操作回传表单（对齐 PushMessageOperateForm，CLICK 等预留扩展）。
    /// </summary>
    public sealed class PushMessageOperateForm
    {
        [JsonPropertyName("messageId")]
        public long? MessageId { get; set; }

        /// <summary>操作类型（如 CLICK）。</summary>
        [JsonPropertyName("opType")]
        public string OpType { get; set; }

        /// <summary>扩展信息（可选）。</summary>
        [JsonPropertyName("extInfo")]
        public string ExtInfo { get; set; }
    }
}
