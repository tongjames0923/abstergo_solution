import '../auth/token_store.dart';
import 'ajax_response.dart';
import 'request_options.dart';

/// 无感刷新协调器（单飞）
///
/// 对齐前端 app-ui-vue request.js 的单飞刷新队列语义：并发请求同时命中
/// 刷新码时只发起一次刷新，全部等待同一结果；成功后由 HTTP 层重放原请求，
/// 失败则触发会话失效。刷新请求自身携带 isRefreshRequest 标记防"排队等自己"死锁。
class SessionRefreshCoordinator {
  /// 原始刷新执行函数（返回携带新令牌的响应信封）
  final Future<AjaxResponse> Function() perform;

  /// 令牌存储（刷新成功后写入新访问令牌）
  final TokenStore tokenStore;

  Future<bool>? _pending;

  /// 创建刷新协调器
  ///
  /// @param perform    刷新执行函数
  /// @param tokenStore 令牌存储
  SessionRefreshCoordinator({required this.perform, required this.tokenStore});

  /// 触发单飞刷新
  ///
  /// @return 刷新成功且新令牌已写入存储时为 true
  Future<bool> refresh() {
    final pending = _pending;
    if (pending != null) {
      return pending;
    }
    final future = _doRefresh().whenComplete(() => _pending = null);
    _pending = future;
    return future;
  }

  /// 执行刷新请求并落库新令牌
  ///
  /// @return 刷新成功时为 true
  Future<bool> _doRefresh() async {
    try {
      final response = await perform();
      final token = response.dataAsMap()['token'] as String?;
      if (response.isSuccess && token != null && token.isNotEmpty) {
        await tokenStore.write(token);
        return true;
      }
      return false;
    } catch (_) {
      return false;
    }
  }
}

/// 刷新请求专用选项（短超时 + 防死锁标记）
///
/// @return 标记 isRefreshRequest、10 秒读取超时的请求选项
TbsRequestOptions refreshRequestOptions() => const TbsRequestOptions(
      isRefreshRequest: true,
      receiveTimeout: Duration(seconds: 10),
    );
