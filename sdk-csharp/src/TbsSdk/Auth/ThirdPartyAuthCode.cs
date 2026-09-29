using System;
using System.Globalization;
using System.Security.Cryptography;
using System.Text;

namespace TbsSdk.Auth
{
    /// <summary>
    /// 第三方应用授权码生成器（对齐后端 DefaultThirdPartyGrantProvider.codeGenerate）。
    /// <para>算法：SHA-256(SALT:appSecret:timestamp:appId) 转小写十六进制；
    /// 服务端以相同算法重建授权码比对，时间戳有效窗口 ±3 分钟。</para>
    /// </summary>
    public static class ThirdPartyAuthCode
    {
        /// <summary>授权码生成盐值（与后端固定常量一致，禁止修改）。</summary>
        public const string Salt = "cad39065478d1e81fc528f9a6be211a5";

        /// <summary>
        /// 生成第三方登录授权码。
        /// </summary>
        /// <param name="appId">第三方应用标识。</param>
        /// <param name="appSecret">第三方应用密钥。</param>
        /// <param name="timestamp">毫秒时间戳（与请求体 timestamp 一致）。</param>
        /// <returns>64 位小写十六进制授权码。</returns>
        public static string Generate(string appId, string appSecret, long timestamp)
        {
            if (string.IsNullOrEmpty(appId))
            {
                throw new ArgumentException("appId 不能为空", nameof(appId));
            }
            if (appSecret == null)
            {
                throw new ArgumentException("appSecret 不能为空", nameof(appSecret));
            }

            var baseString = Salt + ":" + appSecret + ":" +
                             timestamp.ToString(CultureInfo.InvariantCulture) + ":" + appId;
            return Sha256Hex(baseString).ToLowerInvariant();
        }

        /// <summary>
        /// 计算字符串的 SHA-256 大写十六进制值（独立暴露便于校验测试）。
        /// </summary>
        public static string Sha256Hex(string input)
        {
            using (var sha256 = SHA256.Create())
            {
                var hash = sha256.ComputeHash(Encoding.UTF8.GetBytes(input ?? string.Empty));
                var builder = new StringBuilder(hash.Length * 2);
                foreach (var item in hash)
                {
                    builder.Append(item.ToString("X2", CultureInfo.InvariantCulture));
                }
                return builder.ToString();
            }
        }
    }
}
