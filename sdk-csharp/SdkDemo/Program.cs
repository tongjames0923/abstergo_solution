// 推送 SDK 联调测试：登录 → SSE 建连订阅 → 收信令/帧 → HTTP 拉取 → 标记已读
// 运行后按提示交互：f=拉取消息  n=发送测试通知  +topic=动态增订  -topic=退订  q=退出
using System.Text.Json;
using TbsSdk;
using TbsSdk.Configuration;
using TbsSdk.Models;
using TbsSdk.Push;

var consoleLock = new object();

void Log(string msg)
{
    lock (consoleLock)
    {
        Console.WriteLine($"[{DateTime.Now:HH:mm:ss.fff}] {msg}");
    }
}

using var client = new TbsSdkClient(new TbsSdkOptions
{
    BaseUrl = "http://192.168.80.32:8082",
    // 打开 SDK 内部诊断日志，便于观察建连/换票/上行失败等流程
    OnLog = (level, message) => Log($"[log:{level}] {message}")
});

// 会话失效（405/468）：本 demo 直接提示，真实宿主应引导重新登录
client.SessionExpired += (s, e) => Log("会话失效，请重新登录: " + e.Message);

// ---- 1. 登录并取用户数字 id（SSE 缺省订阅 per-user SIGNAL 频道需要） ----
Log("正在登录...");
var token = await client.LoginAsync("01505", "01505");
Log($"登录成功: userName={token.UserName}, tokenType={token.TokenType}");

var user = await client.Auth.GetUserInfoAsync();
var userId = user.Id?.ToString();
Log($"当前用户: id={userId}, nick={user.NickName}");

if (string.IsNullOrEmpty(userId))
{
    Log("未取到用户数字 id，无法确定缺省 SIGNAL 频道，请显式传入主题后再试。退出。");
    return;
}

// ---- 2. 挂载推送事件（对齐后端 signal+拉取新链路） ----
client.Push.StatusChanged += status => Log($"连接状态 → {status}");

// Hello/SignalReceived 事件委托为 void，回调内含 await 需标记 async（async void 发射后不管）
client.Push.Hello += async hello =>
{
    Log($"握手完成: sessionId={hello.SessionId}, version={hello.Version}");
    // 连接就绪先拉一次，补齐离线期间消息
    await FetchAsync("hello 兜底");
};

// signal 无载荷，仅提示"频道有新消息"，触发 HTTP 拉取
client.Push.SignalReceived += async () =>
{
    Log("收到 SIGNAL 信令，触发消息拉取...");
    await FetchAsync("signal");
};

// data/pong/订阅退订确认等原始帧统一走这里
client.Push.FrameReceived += frame =>
{
    if (frame == null) return;
    Log($"下行帧: type={frame.Type}, topic={frame.Topic}, messageId={frame.MessageId}, payload={frame.Payload}");
};

client.Push.ErrorOccurred += ex => Log($"连接异常(将换票重连): {ex.Message}");

// ---- 3. 启动推送连接（认证换票 → SSE 建连 → 订阅 → 心跳保活） ----
Log("正在建立推送连接...");
client.StartPush(userId);

// ---- 4. 交互命令循环 ----
Log("命令: [f]拉取  [n]发送测试通知  [+主题]动态增订  [-主题]退订  [q]退出");
while (true)
{
    var key = Console.ReadKey(true).KeyChar;
    switch (char.ToLowerInvariant(key))
    {
        case 'f':
            await FetchAsync("手动");
            break;
        case 'n':
            await SendNotifyAsync();
            break;
        case 'q':
            Log("正在退出...");
            client.Push.Close();
            return;
        case '+':
        case '-':
            var topic = ReadLine("主题");
            if (string.IsNullOrWhiteSpace(topic)) break;
            if (key == '+') client.Push.SubscribeTopics(topic);
            else client.Push.UnsubscribeTopics(topic);
            Log($"当前订阅: {string.Join(", ", client.Push.Topics)}");
            break;
    }
}

// ---- 本地函数 ----

async Task FetchAsync(string trigger)
{
    try
    {
        var unread = await client.Messages.FetchUnreadCountAsync();
        Log($"[{trigger}] 未读数 = {unread}");

        var page = await client.Messages.FetchMessagesAsync(limit: 20);
        var ids = new List<long>();
        foreach (var m in page.List)
        {
            Log($"  消息 #{m.MessageId} [{m.MsgType}/{m.Priority}] {m.Title} - {m.Content} (已读={m.ReadFlag})");
            if (m.ReadFlag != true && m.MessageId.HasValue)
            {
                ids.Add(m.MessageId.Value);
            }
        }

        if (ids.Count > 0)
        {
            var affected = await client.Messages.MarkReadAsync(ids);
            Log($"[{trigger}] 标记已读 {ids.Count} 条，实际记录 {affected} 条");
        }
    }
    catch (Exception ex)
    {
        Log($"[{trigger}] 拉取失败: {ex.Message}");
    }
}

async Task SendNotifyAsync()
{
    try
    {
        // 注意：接收人由后端 INotifyReceiverResolver 解析（默认空集=无人接收），
        // 若信令未回到本端属正常，需后端解析器将当前用户纳入接收人。
        var form = new PushNotifySendForm
        {
            MsgType = PushMessageType.Notify,
            Title = "SDK 测试通知",
            Content = "来自 SdkDemo 的一条测试消息 " + DateTime.Now,
            Priority = PushMessagePriority.High
        };
        var result = await client.Messages.SendNotifyAsync(form);
        Log($"发送通知: messageId={result?.MessageId}, topic={result?.Topic}, " +
            $"receiverCount={result?.ReceiverCount}, signalCount={result?.SignalCount}");
    }
    catch (Exception ex)
    {
        Log($"发送通知失败: {ex.Message}");
    }
}

string ReadLine(string prompt)
{
    Console.Write($"\n请输入{prompt}: ");
    return Console.ReadLine() ?? string.Empty;
}
