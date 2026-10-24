/// Abstergo Solution 纯 Dart 客户端 SDK
///
/// 统一封装后端 HTTP 协议（AjaxResult 信封 + 错误码分流 + 无感刷新）、
/// 多授权登录、SSE 推送订阅、消息中心、应用更新与动态视图协议，
/// 能力对齐仓库 Java（sdk/）与 C#（sdk-csharp/）客户端家族。
/// 零 Flutter 依赖，可在任意 Dart 环境使用；Flutter 应用建议配合
/// 平台化 TokenStore（安全存储/localStorage）注入。
library;

export 'src/auth/auth_api.dart';
export 'src/auth/third_party_auth_code.dart';
export 'src/auth/token_store.dart';
export 'src/config/push_options.dart';
export 'src/config/session_policy.dart';
export 'src/config/tbs_client_config.dart';
export 'src/constants/tbs_constants.dart';
export 'src/exception/tbs_exceptions.dart';
export 'src/http/ajax_response.dart';
export 'src/http/base_http_client.dart';
export 'src/http/client_events.dart';
export 'src/http/request_options.dart';
export 'src/model/app_update_models.dart';
export 'src/model/frame_type.dart';
export 'src/model/json_utils.dart';
export 'src/model/login_grant_model.dart';
export 'src/model/push_credential.dart';
export 'src/model/push_frame.dart';
export 'src/model/push_message_models.dart';
export 'src/model/token_model.dart';
export 'src/model/user_info_model.dart';
export 'src/push/push_message_api.dart';
export 'src/push/sse_frame_codec.dart';
export 'src/push/sse_push_subscriber.dart';
export 'src/tbs_client.dart';
export 'src/update/update_api.dart';
export 'src/view/paged_api.dart';
export 'src/view/view_api.dart';
export 'src/view/view_models.dart';
