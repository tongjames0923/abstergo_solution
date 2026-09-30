using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace TbsSdk.Update
{
    /// <summary>
    /// 升级动作类型常量（对齐后端 AppVersionCompatibilityActionEnum 的 code 值）。
    /// </summary>
    public static class AppUpdateAction
    {
        /// <summary>强制更新：低于最低兼容版本时阻断使用直至升级。</summary>
        public const int Force = 1;

        /// <summary>推荐更新：提示用户升级但不阻断使用。</summary>
        public const int Recommend = 2;

        /// <summary>仅提示：告知用户存在新版本，不做进一步干预。</summary>
        public const int Notify = 3;

        /// <summary>动作编码转可读名称（force/recommend/notify），未知值返回 null。</summary>
        public static string ToName(int? action)
        {
            switch (action)
            {
                case Force: return "force";
                case Recommend: return "recommend";
                case Notify: return "notify";
                default: return null;
            }
        }
    }

    /// <summary>
    /// 客户端平台常量（对齐后端升级检查入参 platform 约定，如 ANDROID、IOS、WEB）。
    /// </summary>
    public static class AppPlatform
    {
        public const string Android = "ANDROID";
        public const string Ios = "IOS";
        public const string Web = "WEB";
        public const string Windows = "WINDOWS";
        public const string MacOs = "MACOS";
        public const string Linux = "LINUX";
    }

    /// <summary>
    /// 升级检查请求体（对齐后端 AppVersionCheckForm）。
    /// </summary>
    public sealed class AppUpdateCheckForm
    {
        /// <summary>应用标识。</summary>
        [JsonPropertyName("appKey")]
        public string AppKey { get; set; }

        /// <summary>平台标识（如 ANDROID/IOS/WEB，见 <see cref="AppPlatform"/>）。</summary>
        [JsonPropertyName("platform")]
        public string Platform { get; set; }

        /// <summary>客户端当前内部版本号。</summary>
        [JsonPropertyName("currentVersionCode")]
        public long? CurrentVersionCode { get; set; }
    }

    /// <summary>
    /// 升级检查结果（对齐后端 AppVersionUpdateCheckVO）。
    /// <para>服务端综合最新已发布版本与生效的兼容策略，返回升级动作强度与下载地址
    /// （OSS 预签名 URL 或外部托管链接，可能存在多个安装包地址）。</para>
    /// </summary>
    public sealed class AppUpdateCheckResult
    {
        /// <summary>是否需要升级。</summary>
        [JsonPropertyName("needUpdate")]
        public bool? NeedUpdate { get; set; }

        /// <summary>升级动作类型（1=force / 2=recommend / 3=notify），不需要升级时为空。</summary>
        [JsonPropertyName("action")]
        public int? Action { get; set; }

        /// <summary>最新已发布版本的内部版本号。</summary>
        [JsonPropertyName("latestVersionCode")]
        public long? LatestVersionCode { get; set; }

        /// <summary>最新已发布版本的外部版本名称。</summary>
        [JsonPropertyName("latestVersionName")]
        public string LatestVersionName { get; set; }

        /// <summary>最新版本的更新日志。</summary>
        [JsonPropertyName("releaseNote")]
        public string ReleaseNote { get; set; }

        /// <summary>最新版本安装包的下载地址列表。</summary>
        [JsonPropertyName("downloadUrls")]
        public List<string> DownloadUrls { get; set; }

        /// <summary>升级动作可读名称（force/recommend/notify），不需要升级时为 null。</summary>
        public string ActionName => AppUpdateAction.ToName(Action);
    }

    /// <summary>
    /// 安装包视图（对齐后端 AppPackageVO，来源于管理端接口，含完整性校验信息）。
    /// </summary>
    public sealed class AppPackageVO
    {
        [JsonPropertyName("id")]
        public long? Id { get; set; }

        /// <summary>所属应用版本 ID。</summary>
        [JsonPropertyName("versionId")]
        public long? VersionId { get; set; }

        /// <summary>对象存储桶名。</summary>
        [JsonPropertyName("bucketName")]
        public string BucketName { get; set; }

        /// <summary>对象存储对象名。</summary>
        [JsonPropertyName("objectName")]
        public string ObjectName { get; set; }

        /// <summary>安装包文件 MD5 值。</summary>
        [JsonPropertyName("md5")]
        public string Md5 { get; set; }

        /// <summary>下载地址。</summary>
        [JsonPropertyName("downloadUrl")]
        public string DownloadUrl { get; set; }

        [JsonPropertyName("createTime")]
        public System.DateTime? CreateTime { get; set; }
    }
}
