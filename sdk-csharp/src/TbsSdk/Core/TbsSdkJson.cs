using System;
using System.Globalization;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace TbsSdk.Core
{
    /// <summary>
    /// SDK 统一 JSON 序列化配置。
    /// <para>后端 Spring Jackson 默认输出 camelCase，故写入侧使用 CamelCase 命名策略、
    /// 读取侧大小写不敏感；时间字段同时兼容数值毫秒时间戳与字符串日期。</para>
    /// </summary>
    public static class TbsSdkJson
    {
        /// <summary>默认序列化选项（线程安全，可全局复用）。</summary>
        public static JsonSerializerOptions DefaultOptions { get; } = CreateDefaultOptions();

        private static JsonSerializerOptions CreateDefaultOptions()
        {
            var options = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                PropertyNameCaseInsensitive = true,
                DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull
            };
            options.Converters.Add(new FlexibleDateTimeConverter());
            return options;
        }
    }

    /// <summary>
    /// 可空时间字段转换器：读取时兼容数值毫秒时间戳与多种字符串日期格式，
    /// 写入时输出 "yyyy-MM-dd HH:mm:ss"。对齐前端对 createTime 的多格式解析逻辑。
    /// </summary>
    public sealed class FlexibleDateTimeConverter : JsonConverter<DateTime?>
    {
        private static readonly string[] ExactFormats =
        {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-ddTHH:mm:ss",
            "yyyy-MM-dd"
        };

        public override DateTime? Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            if (reader.TokenType == JsonTokenType.Number)
            {
                if (reader.TryGetInt64(out var millis))
                {
                    return DateTimeOffset.FromUnixTimeMilliseconds(millis).LocalDateTime;
                }
                return null;
            }

            if (reader.TokenType == JsonTokenType.String)
            {
                var text = reader.GetString();
                if (string.IsNullOrEmpty(text))
                {
                    return null;
                }
                if (DateTime.TryParse(text, CultureInfo.InvariantCulture, DateTimeStyles.None, out var parsed))
                {
                    return parsed;
                }
                if (DateTime.TryParseExact(text, ExactFormats, CultureInfo.InvariantCulture, DateTimeStyles.None, out parsed))
                {
                    return parsed;
                }
            }

            return null;
        }

        public override void Write(Utf8JsonWriter writer, DateTime? value, JsonSerializerOptions options)
        {
            if (value.HasValue)
            {
                writer.WriteStringValue(value.Value.ToString("yyyy-MM-dd HH:mm:ss", CultureInfo.InvariantCulture));
            }
            else
            {
                writer.WriteNullValue();
            }
        }
    }
}
