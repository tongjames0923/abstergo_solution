package com.tbs.logging.demo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 独立泛化调用演示服务
 * <p>
 * 方法与类型集合刻意覆盖泛化调用的全部易碎点（无参、同名重载、基本类型、包装类型、POJO、泛型集合、枚举、BigDecimal），
 * 用来验证「仅配置就能把 Dubbo 方法声明成 HTTP 接口」这条链路：网关不需要本接口所在的 jar，
 * 服务方也不需要实现任何本项目自定义契约，只要按标准 Dubbo 语义发布即可。
 * </p>
 * <p>
 * 说明：本接口<b>故意</b>与实现一起放在 {@code logging-impl}（不放 {@code logging-api}），
 * 与「服务接口定义在 api 模块」的常规约束相反。原因就是这条链路的验收标准是网关侧看不到接口类——
 * 一旦进入 api 模块，网关会经依赖闭包拿到 jar，测试的就不再是泛化链路而是类型化链路。
 * 该模块只用于演示，不代表业务服务的推荐分层。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IStandaloneDemoService {

    /**
     * 无参方法，验证不登记入参定义也能调用
     *
     * @return 固定应答 {@code pong}
     */
    String ping();

    /**
     * 单参方法，与下面的两参方法构成同名重载
     *
     * @param message 待回显文本
     * @return 回显结果
     */
    String echo(String message);

    /**
     * 两参方法，验证命名式与位置式两种请求体
     *
     * @param message 待回显文本
     * @param times   重复次数，为空按 1 次
     * @return 重复拼接后的文本
     */
    String echo(String message, Integer times);

    /**
     * 基本类型入参，验证 {@code param_type} 必须登记成关键字 {@code long} 而非 {@code java.lang.Long}
     *
     * @param a 加数
     * @param b 被加数
     * @return 两数之和
     */
    long sum(long a, long b);

    /**
     * POJO 入参与返回，验证服务端按声明类型把 Map 还原成 DTO
     *
     * @param payload 演示载荷，可以为空
     * @return 加工后的载荷；入参为空时返回 {@code null}
     */
    StandaloneDemoVo transform(StandaloneDemoVo payload);

    /**
     * 泛型集合入参，验证登记为擦除类型 {@code java.util.List} 时元素能否还原成 DTO
     *
     * @param payloads 载荷列表，可以为空
     * @param tag      过滤标签，为空表示不过滤
     * @return 命中的载荷列表
     */
    List<StandaloneDemoVo> filter(List<StandaloneDemoVo> payloads, String tag);

    /**
     * 枚举入参，验证请求里传枚举名字符串能否还原成枚举
     *
     * @param status 状态枚举，可以为空
     * @return 枚举名与展示文案的组合结果
     */
    String status(StandaloneDemoStatus status);

    /**
     * 高精度数值入参，验证 JSON 数字与 {@code BigDecimal} 的还原
     *
     * @param amount 金额，可以为空
     * @param scale  小数位数，为空按 2 位
     * @return 四舍五入后的金额
     */
    BigDecimal round(BigDecimal amount, Integer scale);
}
