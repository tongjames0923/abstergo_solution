/// dio 底层适配器装配（native 平台实现：默认 IOHttpClientAdapter 已满足）
library;

/// 装配 dio 底层适配器（native 实现：无调整）
///
/// @param dio             dio 实例
/// @param withCredentials 预留参数（native 不涉及跨域凭证）
void configureHttpClientAdapter(dynamic dio, {bool withCredentials = false}) {}
