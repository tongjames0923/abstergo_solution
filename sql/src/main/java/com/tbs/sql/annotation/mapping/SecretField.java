package com.tbs.sql.annotation.mapping;

import java.lang.annotation.*;

/**
 * 安全字段标记注解。
 *
 * <p>该注解用于标记敏感字段，在日志记录或数据输出时可以自动脱敏处理。
 * 标记了此注解的字段在输出时会被替换为星号或其他脱敏字符。</p>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>标记密码、身份证号、手机号等敏感信息字段</li>
 *   <li>在日志记录时自动脱敏</li>
 *   <li>在数据导出时保护敏感信息</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * public class User {
 *     @SecretField
 *     private String password;
 *     
 *     @SecretField
 *     private String idCard;
 *     
 *     @SecretField
 *     private String phone;
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>该注解只能用于字段级别</li>
 *   <li>需要配合日志记录器或序列化工具使用</li>
 *   <li>具体脱敏逻辑由使用方实现</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see com.tbs.sql.logger.ISqlLogger
 */
@Target(ElementType.FIELD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SecretField {
}
