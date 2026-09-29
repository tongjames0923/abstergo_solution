using System.Collections.Generic;
using System.Text.Json;

namespace TbsSdk.Core
{
    /// <summary>
    /// 后端统一响应体 AjaxResult 的 C# 映射。
    /// <para>固定键：code(int)/message(string)/data(业务体)；其余键（redirect/type/duration/title 等）
    /// 归入扩展字典，可通过 <see cref="TryGet{T}"/> / <see cref="GetString"/> 按需读取。</para>
    /// </summary>
    public sealed class AjaxResult
    {
        private readonly IDictionary<string, JsonElement> _extra;

        private AjaxResult(int? code, string message, JsonElement data, bool hasData, JsonElement raw,
            IDictionary<string, JsonElement> extra)
        {
            Code = code;
            Message = message;
            Data = data;
            HasData = hasData;
            Raw = raw;
            _extra = extra;
        }

        /// <summary>业务码（缺失时为 null）。</summary>
        public int? Code { get; }

        /// <summary>业务消息。</summary>
        public string Message { get; }

        /// <summary>业务体元素（缺失时为 default，可用 <see cref="HasData"/> 判定）。</summary>
        public JsonElement Data { get; }

        /// <summary>业务体是否存在于响应中（data 为 null 时为 true 但元素为 Null）。</summary>
        public bool HasData { get; }

        /// <summary>响应体根元素（整包，便于取扩展字段）。</summary>
        public JsonElement Raw { get; }

        /// <summary>redirect 附加字段（存在时由调用方自行跳转/处理）。</summary>
        public string Redirect => GetString("redirect");

        /// <summary>type 附加字段（消息展示类型，如 success/error/info）。</summary>
        public string Type => GetString("type");

        /// <summary>title 附加字段。</summary>
        public string Title => GetString("title");

        /// <summary>duration 附加字段（展示时长毫秒）。</summary>
        public int? Duration => TryGet<int>("duration", out var value) ? value : (int?)null;

        /// <summary>
        /// 将响应体解析为 <see cref="AjaxResult"/>；非 JSON 对象时 Code 为 null。
        /// </summary>
        public static AjaxResult Parse(JsonElement root)
        {
            var extra = new Dictionary<string, JsonElement>();
            int? code = null;
            string message = null;
            var data = default(JsonElement);
            var hasData = false;

            if (root.ValueKind == JsonValueKind.Object)
            {
                foreach (var property in root.EnumerateObject())
                {
                    switch (property.Name)
                    {
                        case "code":
                            if (property.Value.ValueKind == JsonValueKind.Number && property.Value.TryGetInt32(out var codeValue))
                            {
                                code = codeValue;
                            }
                            else if (property.Value.ValueKind == JsonValueKind.String &&
                                     int.TryParse(property.Value.GetString(), out var parsedCode))
                            {
                                code = parsedCode;
                            }
                            break;
                        case "message":
                            message = property.Value.ValueKind == JsonValueKind.String
                                ? property.Value.GetString()
                                : property.Value.ToString();
                            break;
                        case "data":
                            data = property.Value.Clone();
                            hasData = true;
                            break;
                        default:
                            extra[property.Name] = property.Value.Clone();
                            break;
                    }
                }
            }

            return new AjaxResult(code, message, data, hasData, root.Clone(), extra);
        }

        /// <summary>
        /// 将业务体反序列化为指定类型；业务体缺失或为 null 时返回 default。
        /// </summary>
        public T GetData<T>(JsonSerializerOptions options = null)
        {
            if (!HasData || Data.ValueKind == JsonValueKind.Null || Data.ValueKind == JsonValueKind.Undefined)
            {
                return default;
            }
            return JsonSerializer.Deserialize<T>(Data.Clone(), options ?? TbsSdkJson.DefaultOptions);
        }

        /// <summary>读取扩展字段并反序列化。</summary>
        public bool TryGet<T>(string name, out T value)
        {
            value = default;
            if (_extra == null || !_extra.TryGetValue(name, out var element) ||
                element.ValueKind == JsonValueKind.Null || element.ValueKind == JsonValueKind.Undefined)
            {
                return false;
            }

            try
            {
                value = JsonSerializer.Deserialize<T>(element.Clone(), TbsSdkJson.DefaultOptions);
                return true;
            }
            catch (JsonException)
            {
                return false;
            }
        }

        /// <summary>读取字符串扩展字段。</summary>
        public string GetString(string name)
        {
            return TryGet<string>(name, out var value) ? value : null;
        }
    }
}
