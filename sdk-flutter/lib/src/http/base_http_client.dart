import 'ajax_response.dart';
import 'client_events.dart';
import 'request_options.dart';

/// 统一 HTTP 客户端契约
///
/// 对齐 Java 客户端 IBaseHttpClient：统一解析服务端 AjaxResult 结构并按业务码
/// 映射异常与事件，宿主仅感知成功数据与强类型异常/事件流。
abstract class BaseHttpClient {
  /// 通用请求入口
  ///
  /// @param method HTTP 动词（GET/POST/PUT/DELETE）
  /// @param path   接口路径（相对 baseUrl）
  /// @param body   请求体（自动 JSON 序列化）
  /// @param query  查询参数
  /// @param options 请求级选项
  /// @return 解析后的统一响应信封（仅成功码返回，失败抛 TbsException）
  Future<AjaxResponse> send(
    String method,
    String path, {
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  });

  /// GET 请求
  ///
  /// @param path    接口路径
  /// @param query   查询参数
  /// @param options 请求级选项
  /// @return 统一响应信封
  Future<AjaxResponse> get(
    String path, {
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  });

  /// POST 请求
  ///
  /// @param path    接口路径
  /// @param body    请求体
  /// @param query   查询参数
  /// @param options 请求级选项
  /// @return 统一响应信封
  Future<AjaxResponse> post(
    String path, {
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  });

  /// PUT 请求
  ///
  /// @param path    接口路径
  /// @param body    请求体
  /// @param options 请求级选项
  /// @return 统一响应信封
  Future<AjaxResponse> put(
    String path, {
    Object? body,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  });

  /// DELETE 请求
  ///
  /// @param path    接口路径
  /// @param body    请求体
  /// @param query   查询参数
  /// @param options 请求级选项
  /// @return 统一响应信封
  Future<AjaxResponse> delete(
    String path, {
    Object? body,
    Map<String, dynamic>? query,
    TbsRequestOptions options = TbsRequestOptions.defaults,
  });

  /// 流式下载文件（先写 .part 临时文件，成功后原子重命名）
  ///
  /// @param url        下载地址（完整 URL，通常来自升级检查结果的 downloadUrls）
  /// @param savePath   目标文件路径
  /// @param onProgress 进度回调（received/total 字节数，total 未知时为 -1）
  /// @param headers    追加请求头
  Future<void> download(
    String url,
    String savePath, {
    void Function(int received, int total)? onProgress,
    Map<String, String> headers = const {},
  });

  /// 当前登录令牌（内存镜像）
  ///
  /// @return 令牌串，未登录时为 null
  String? get currentToken;

  /// 客户端事件流（会话失效/通知/跳转）
  Stream<TbsClientEvent> get events;

  /// 释放连接资源（幂等）
  Future<void> close();
}
