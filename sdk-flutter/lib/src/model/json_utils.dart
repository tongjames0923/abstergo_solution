/// 服务端 Long 字段 JSON 解析工具
///
/// 对齐服务端 JacksonCustomConfig 契约：全局 Long 序列化为字符串
/// （ToStringSerializer，防 JS 端雪花 ID 精度丢失），反序列化兼容字符串与
/// 数字两种形态（ToStringOrNumberDeserializer）。本工具为 Dart 侧对等实现：
/// ID 类字段一律以字符串承载（避开 Web 端 53 位精度边界），计数/版本类
/// 字段以 int 承载并双形态兼容。
library;

/// 解析服务端 Long/ID 字段为字符串（兼容字符串与数字形态）
///
/// @param value 服务端字段值
/// @return 字符串形态，缺失时为 null
String? parseFlexibleLong(dynamic value) {
  if (value == null) {
    return null;
  }
  if (value is String) {
    return value.isEmpty ? null : value;
  }
  if (value is num) {
    return value.toString();
  }
  return null;
}

/// 解析服务端数值字段为 int（兼容字符串与数字形态）
///
/// @param value 服务端字段值
/// @return int 形态，缺失或不可解析时为 null
int? parseFlexibleInt(dynamic value) {
  if (value == null) {
    return null;
  }
  if (value is num) {
    return value.toInt();
  }
  if (value is String) {
    return int.tryParse(value);
  }
  return null;
}
