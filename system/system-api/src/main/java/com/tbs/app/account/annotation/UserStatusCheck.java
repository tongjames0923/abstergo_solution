package com.tbs.app.account.annotation;

import com.tbs.app.account.IAccountStatusValidator;

import java.lang.annotation.*;

/**
 * 用户状态检查注解
 * <p>
 * 该注解用于标识需要检查用户状态的接口方法。 可以直接标注在方法上，也可以标注在其他注解上实现组合使用。
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 直接在方法上使用
 * @UserStatusCheck(MyValidator.class)
 * public void someMethod() { ... }
 *
 * // 指定参数名和错误信息
 * @UserStatusCheck(validator = MyValidator.class, accountKey = "accountVO", message = "用户状态异常")
 * public void anotherMethod(AccountVO accountVO) { ... }
 *
 * // 多个验证器
 * @UserStatusCheck(MyValidator.class)
 * @UserStatusCheck(AnotherValidator.class)
 * public void multiCheckMethod() { ... }
 * </pre>
 * </p>
 *
 * @author abstergo
 * @see IAccountStatusValidator
 * @see UserStatusChecks
 */
@Repeatable(value = UserStatusChecks.class)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Inherited
@Documented
public @interface UserStatusCheck {

    /**
     * 指定账户状态验证器实现类
     * <p>
     * 该验证器必须实现 {@link IAccountStatusValidator} 接口。
     * </p>
     *
     * @return 验证器实现类
     */
    Class<? extends IAccountStatusValidator> validator();

}
