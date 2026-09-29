using System;
using System.IO;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using TbsSdk.Core;

namespace TbsSdk.Update
{
    /// <summary>
    /// 应用更新模块：客户端升级检查与安装包下载。
    /// <para>
    /// 升级检查走公开端点 POST /public/app-update/check（无需登录）；服务端综合最新已发布版本
    /// 与生效的兼容策略返回升级动作强度（force/recommend/notify）与安装包下载地址。
    /// 下载地址为 OSS 预签名 URL 或外部托管链接，下载使用独立 HttpClient（不受 API 超时限制），
    /// 支持进度回调与取消。
    /// </para>
    /// </summary>
    public sealed class AppUpdateClient : IDisposable
    {
        /// <summary>公开升级检查端点。</summary>
        public const string PublicCheckPath = "/public/app-update/check";

        /// <summary>管理端升级检查端点（需 API.DEV.VERSION.POLICY.QUERY 权限）。</summary>
        public const string AdminCheckPath = "/private/admin/app-version-policy/check";

        private readonly TbsHttpClient _http;
        private readonly HttpClient _downloadHttpClient;
        private readonly bool _ownsDownloadHttpClient;

        /// <summary>创建更新模块。</summary>
        /// <param name="http">HTTP 核心（升级检查复用）。</param>
        /// <param name="downloadHttpClient">外部注入的下载 HttpClient（可选，用于代理/测试）。</param>
        public AppUpdateClient(TbsHttpClient http, HttpClient downloadHttpClient = null)
        {
            _http = http ?? throw new ArgumentNullException(nameof(http));
            if (downloadHttpClient != null)
            {
                _downloadHttpClient = downloadHttpClient;
                _ownsDownloadHttpClient = false;
            }
            else
            {
                // 下载耗时远超 API 请求，独立客户端并解除整体超时限制（逐读取消由 CancellationToken 负责）
                _downloadHttpClient = new HttpClient { Timeout = System.Threading.Timeout.InfiniteTimeSpan };
                _ownsDownloadHttpClient = true;
            }
        }

        /// <summary>
        /// 客户端升级检查（公开端点，无需登录）。
        /// </summary>
        /// <param name="appKey">应用标识。</param>
        /// <param name="platform">平台（如 ANDROID/IOS/WEB，见 <see cref="AppPlatform"/>）。</param>
        /// <param name="currentVersionCode">客户端当前内部版本号。</param>
        /// <returns>升级检查结果：needUpdate、action 强度与 downloadUrls。</returns>
        public Task<AppUpdateCheckResult> CheckUpgradeAsync(string appKey, string platform,
            long currentVersionCode, CancellationToken cancellationToken = default)
        {
            return CheckUpgradeAsync(PublicCheckPath, appKey, platform, currentVersionCode, cancellationToken);
        }

        /// <summary>
        /// 升级检查（指定端点）：公开端点或管理端 `/private/admin/app-version-policy/check` 均可传入。
        /// </summary>
        public async Task<AppUpdateCheckResult> CheckUpgradeAsync(string path, string appKey, string platform,
            long currentVersionCode, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrEmpty(appKey))
            {
                throw new ArgumentException("appKey 不能为空", nameof(appKey));
            }
            if (string.IsNullOrEmpty(platform))
            {
                throw new ArgumentException("platform 不能为空", nameof(platform));
            }

            var form = new AppUpdateCheckForm
            {
                AppKey = appKey,
                Platform = platform,
                CurrentVersionCode = currentVersionCode
            };
            var result = await _http.PostAsync(path, form, null, null, cancellationToken).ConfigureAwait(false);
            return result.GetData<AppUpdateCheckResult>() ?? new AppUpdateCheckResult();
        }

        /// <summary>
        /// 下载安装包到指定文件（支持进度回调与取消；异常时删除不完整文件）。
        /// </summary>
        /// <param name="downloadUrl">安装包下载地址（升级检查结果中的 downloadUrls 项）。</param>
        /// <param name="destinationPath">目标文件完整路径。</param>
        /// <param name="progress">进度回调：已下载字节数与总字节数（总长未知时为 -1）。</param>
        /// <returns>下载到磁盘的总字节数。</returns>
        public async Task<long> DownloadPackageAsync(string downloadUrl, string destinationPath,
            IProgress<DownloadProgress> progress = null, CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrEmpty(downloadUrl))
            {
                throw new ArgumentException("downloadUrl 不能为空", nameof(downloadUrl));
            }
            if (string.IsNullOrEmpty(destinationPath))
            {
                throw new ArgumentException("destinationPath 不能为空", nameof(destinationPath));
            }

            var directory = Path.GetDirectoryName(Path.GetFullPath(destinationPath));
            if (!string.IsNullOrEmpty(directory) && !Directory.Exists(directory))
            {
                Directory.CreateDirectory(directory);
            }

            try
            {
                using (var response = await _downloadHttpClient
                    .GetAsync(downloadUrl, HttpCompletionOption.ResponseHeadersRead, cancellationToken)
                    .ConfigureAwait(false))
                {
                    response.EnsureSuccessStatusCode();

                    long? totalBytes = null;
                    if (response.Content.Headers.ContentLength.HasValue)
                    {
                        totalBytes = response.Content.Headers.ContentLength.Value;
                    }

                    using (var source = await response.Content.ReadAsStreamAsync().ConfigureAwait(false))
                    using (var target = new FileStream(destinationPath, FileMode.Create, FileAccess.Write,
                        FileShare.None, 81920, useAsync: true))
                    {
                        var buffer = new byte[81920];
                        long downloaded = 0;
                        while (true)
                        {
                            var read = await source.ReadAsync(buffer, 0, buffer.Length, cancellationToken)
                                .ConfigureAwait(false);
                            if (read == 0)
                            {
                                break;
                            }
                            await target.WriteAsync(buffer, 0, read, cancellationToken).ConfigureAwait(false);
                            downloaded += read;
                            ReportProgress(progress, downloaded, totalBytes);
                        }
                        return downloaded;
                    }
                }
            }
            catch
            {
                // 下载失败时清理不完整文件，避免宿主误用半包
                try
                {
                    if (File.Exists(destinationPath))
                    {
                        File.Delete(destinationPath);
                    }
                }
                catch (IOException)
                {
                    // 清理失败不掩盖原始异常
                }
                throw;
            }
        }

        private static void ReportProgress(IProgress<DownloadProgress> progress, long downloaded, long? totalBytes)
        {
            if (progress == null)
            {
                return;
            }
            progress.Report(new DownloadProgress(downloaded, totalBytes ?? -1));
        }

        public void Dispose()
        {
            if (_ownsDownloadHttpClient)
            {
                _downloadHttpClient.Dispose();
            }
        }
    }

    /// <summary>
    /// 下载进度（已下载字节数与总字节数，总长未知时 TotalBytes 为 -1）。
    /// </summary>
    public sealed class DownloadProgress
    {
        public DownloadProgress(long downloadedBytes, long totalBytes)
        {
            DownloadedBytes = downloadedBytes;
            TotalBytes = totalBytes;
        }

        /// <summary>已下载字节数。</summary>
        public long DownloadedBytes { get; }

        /// <summary>总字节数（服务端未返回 Content-Length 时为 -1）。</summary>
        public long TotalBytes { get; }

        /// <summary>进度百分比（0~1，总长未知时为 null）。</summary>
        public double? Percent => TotalBytes > 0 ? (double)DownloadedBytes / TotalBytes : (double?)null;
    }
}
