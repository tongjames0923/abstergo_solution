/// 适配器装配（不支持平台桩实现）
library;

/// 装配 dio 底层适配器（桩实现：无调整）
///
/// @param dio             dio 实例
/// @param withCredentials 是否携带跨域凭证
void configureHttpClientAdapter(dynamic dio, {bool withCredentials = false}) {}
