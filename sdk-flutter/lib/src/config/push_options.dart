/// SSE 推送连接参数
///
/// 缺省值对齐前端 app-ui-vue settings.push 配置（心跳 200s 须小于服务端
/// 600s 空闲超时）；重连间隔 = min(base * 2^已重连次数, max) + [0, jitter) 随机抖动，
/// 连续自动重连超过 [reconnectMaxAttempts] 次仍未成功则停止并通知宿主手动重连。
library;

import 'dart:math';

/// SSE 推送连接参数
class PushOptions {
  /// 随机抖动数源
  static final Random _random = Random();

  /// per-user 信令频道缺省主题前缀（拼接 userId）
  final String topicPrefix;

  /// 心跳上行周期（须远小于服务端空闲超时；非正数表示关闭心跳）
  final Duration heartbeatInterval;

  /// 心跳连续失败阈值：达到即判定会话已死，主动关流换票重连
  final int heartbeatMaxFailures;

  /// 重连退避基数（首次重连等待时长）
  final Duration reconnectBaseDelay;

  /// 重连退避上限（多次失败后单次重连间隔封顶）
  final Duration reconnectMaxDelay;

  /// 重连随机抖动上限（防多端同时重连惊群；非正数表示不叠加抖动）
  final Duration reconnectJitter;

  /// 最大自动重连次数（非正数表示不限次）
  final int reconnectMaxAttempts;

  /// SSE 协议版本（与服务端一致）
  final int protocolVersion;

  /// 创建推送参数
  const PushOptions({
    this.topicPrefix = 'tbs.push.channel.',
    this.heartbeatInterval = const Duration(seconds: 200),
    this.heartbeatMaxFailures = 3,
    this.reconnectBaseDelay = const Duration(seconds: 30),
    this.reconnectMaxDelay = const Duration(seconds: 60),
    this.reconnectJitter = const Duration(seconds: 1),
    this.reconnectMaxAttempts = 3,
    this.protocolVersion = 1,
  });

  /// 计算第 attempt 次重连的退避间隔
  ///
  /// @param attempt 已失败重连次数（从 0 计）
  /// @return 退避间隔（含随机抖动）
  Duration backoffFor(int attempt) {
    final safeAttempt = attempt < 0 ? 0 : attempt;
    final baseMs = reconnectBaseDelay.inMilliseconds;
    final maxMs = reconnectMaxDelay.inMilliseconds;
    final raw = baseMs * (1 << safeAttempt.clamp(0, 30));
    final capped = raw > maxMs ? maxMs : raw;
    final jitterLimit = reconnectJitter.inMilliseconds;
    final jitterMs = jitterLimit <= 0 ? 0 : _random.nextInt(jitterLimit);
    return Duration(milliseconds: capped + jitterMs);
  }
}
