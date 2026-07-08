package com.tbs.shared.interfaces.annoation;

/**
 * 账户状态检查注解（已废弃）
 * <p>
 * 该注解已废弃，请使用 {@link com.tbs.shared.annotation.UserStatusCheck} 代替。
 * </p>
 * <p>
 * 旧注解功能简单，新注解提供了更多功能：
 * <ul>
 * <li>支持自定义错误信息</li>
 * <li>支持指定参数名</li>
 * <li>支持多个验证器</li>
 * <li>支持验证失败时是否抛出异常</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @see com.tbs.shared.annotation.UserStatusCheck
 */
@Deprecated
public @interface AccountStatusCheck {

}
