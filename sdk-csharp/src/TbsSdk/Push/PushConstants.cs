namespace TbsSdk.Push
{
    /// <summary>
    /// 推送协议常量（对齐前端 push/push-message.js 与后端 PushFrameConstants / PushNotifyServiceImpl）。
    /// <para>注意：上行帧 type 为后端 FrameTypeEnum 枚举大写名（SUBSCRIBE/UNSUBSCRIBE/PING），
    /// 下行 SSE 事件名为帧类型小写值（hello/signal/pong/data/error），两套表达勿混用。</para>
    /// </summary>
    public static class PushConstants
    {
        // ---- 端点路径 ----

        /// <summary>连接认证并签发一次性凭证。</summary>
        public const string AuthPath = "/private/push/auth";

        /// <summary>SSE 下行流建连（push-ticket 作 query 参数）。</summary>
        public const string SseConnectPath = "/private/push/sse/connect";

        /// <summary>上行帧端点（订阅/退订/心跳）。</summary>
        public const string SseUploadPath = "/private/push/sse/upload";

        /// <summary>分页拉取当前用户消息列表。</summary>
        public const string MessageListPath = "/private/push/message/list";

        /// <summary>查询当前用户未读数。</summary>
        public const string MessageUnreadCountPath = "/private/push/message/unread-count";

        /// <summary>批量记录已读。</summary>
        public const string MessageReadPath = "/private/push/message/read";

        /// <summary>通用操作回传（CLICK 等预留扩展）。</summary>
        public const string MessageOperatePath = "/private/push/message/operate";

        /// <summary>发送持久化通知（新链路发送入口）。</summary>
        public const string MessageNotifyPath = "/private/push/message/notify";

        // ---- 下行 SSE 事件名（= 帧类型小写值，无 push- 前缀） ----

        public const string EventHello = "hello";
        public const string EventSignal = "signal";
        public const string EventPong = "pong";
        public const string EventData = "data";
        public const string EventError = "error";

        // ---- 上行帧 type（后端 FrameTypeEnum 枚举大写名） ----

        public const string FrameTypeSubscribe = "SUBSCRIBE";
        public const string FrameTypeUnsubscribe = "UNSUBSCRIBE";
        public const string FrameTypePing = "PING";

        // ---- 主题与消息 ----

        /// <summary>per-user SIGNAL 频道主题前缀（拼接用户数字 id 得到频道）。</summary>
        public const string TopicPrefixDefault = "tbs.push.channel.";

        /// <summary>消息读取范围：全部（含已读）。</summary>
        public const string ReadFlagAll = "all";

        /// <summary>消息读取范围：仅未读。</summary>
        public const string ReadFlagUnread = "unread";
    }

    /// <summary>
    /// 消息优先级（对齐前端 PRIORITIES）。
    /// </summary>
    public static class PushMessagePriority
    {
        public const string Low = "low";
        public const string Normal = "normal";
        public const string High = "high";
        public const string Urgent = "urgent";

        /// <summary>判定是否为已知优先级。</summary>
        public static bool IsKnown(string priority)
        {
            return priority == Low || priority == Normal || priority == High || priority == Urgent;
        }
    }

    /// <summary>
    /// 消息类型常量（业务可字典驱动扩展，后端仅校验非空与长度）。
    /// </summary>
    public static class PushMessageType
    {
        public const string Notify = "notify";
        public const string Warning = "warning";
        public const string Reminder = "reminder";
    }

    /// <summary>
    /// 消息操作类型（operate 回传预留扩展）。
    /// </summary>
    public static class PushMessageOperation
    {
        /// <summary>点击。</summary>
        public const string Click = "CLICK";
    }
}
