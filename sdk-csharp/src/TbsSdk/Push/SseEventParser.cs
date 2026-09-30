using System.Collections.Generic;

namespace TbsSdk.Push
{
    /// <summary>
    /// 解析完成的 SSE 事件。
    /// </summary>
    public sealed class SseEvent
    {
        /// <summary>事件名（未指定时按规范为 message）。</summary>
        public string Event { get; set; }

        /// <summary>数据载荷（多行 data 以 \n 连接）。</summary>
        public string Data { get; set; }

        /// <summary>最近一次声明的 id（跨事件持久）。</summary>
        public string LastEventId { get; set; }

        /// <summary>服务端建议的重连间隔（毫秒，可空）。</summary>
        public long? RetryMillis { get; set; }

        /// <summary>是否携带数据。</summary>
        public bool HasData => !string.IsNullOrEmpty(Data);
    }

    /// <summary>
    /// SSE（Server-Sent Events）文本流增量解析器（纯函数式喂行，可独立单测）。
    /// <para>遵循 SSE 规范：event/data/id/retry 字段与空行派发；冒号开头为注释行；
    /// 无 data 的事件不派发；data 多行以 \n 连接；字段值的前导单个空格被剥离。</para>
    /// <para>netstandard2.0 无内置 SSE 客户端，SDK 借助 HttpClient 流式读取 +
    /// 本解析器实现事件流消费（可原生携带 Authorization 头）。</para>
    /// </summary>
    public sealed class SseEventParser
    {
        private readonly List<string> _dataLines = new List<string>();
        private string _eventType;
        private string _lastEventId;
        private long? _retry;

        /// <summary>重置解析状态（新事件流开始时调用）。</summary>
        public void Reset()
        {
            _dataLines.Clear();
            _eventType = null;
            _retry = null;
        }

        /// <summary>
        /// 喂入一行原始文本。
        /// </summary>
        /// <returns>空行结束一个事件时返回完整 <see cref="SseEvent"/>，否则返回 null。</returns>
        public SseEvent FeedLine(string line)
        {
            if (line == null)
            {
                return null;
            }

            // 兼容 \r\n（StreamReader.ReadLine 已剥离 \n，此处兜底 \r）
            if (line.EndsWith("\r"))
            {
                line = line.Substring(0, line.Length - 1);
            }

            // 空行：派发当前缓冲的事件
            if (line.Length == 0)
            {
                return Dispatch();
            }

            // 冒号开头：注释，忽略
            if (line[0] == ':')
            {
                return null;
            }

            string field;
            string value;
            var colonIndex = line.IndexOf(':');
            if (colonIndex < 0)
            {
                field = line;
                value = string.Empty;
            }
            else
            {
                field = line.Substring(0, colonIndex);
                value = line.Substring(colonIndex + 1);
                if (value.StartsWith(" "))
                {
                    value = value.Substring(1);
                }
            }

            switch (field)
            {
                case "event":
                    _eventType = value;
                    break;
                case "data":
                    _dataLines.Add(value);
                    break;
                case "id":
                    _lastEventId = value;
                    break;
                case "retry":
                    if (long.TryParse(value, out var retryValue))
                    {
                        _retry = retryValue;
                    }
                    break;
                // 其他字段按规范忽略
            }

            return null;
        }

        private SseEvent Dispatch()
        {
            // data 缓冲为空时不派发（SSE 规范）
            if (_dataLines.Count == 0)
            {
                _eventType = null;
                _retry = null;
                return null;
            }

            var sseEvent = new SseEvent
            {
                Event = string.IsNullOrEmpty(_eventType) ? "message" : _eventType,
                Data = string.Join("\n", _dataLines),
                LastEventId = _lastEventId,
                RetryMillis = _retry
            };

            _dataLines.Clear();
            _eventType = null;
            _retry = null;
            return sseEvent;
        }
    }
}
