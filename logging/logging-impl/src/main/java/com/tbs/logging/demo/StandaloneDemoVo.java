package com.tbs.logging.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 泛化调用演示载荷
 * <p>
 * 字段组合刻意覆盖「网关只见到 JSON、服务端要还原成对象」的典型形态：字符串、包装数、高精度数、字符串列表。
 * 泛化调用时它在网关侧始终以 Map 形态存在，只有服务端知道这个类。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandaloneDemoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 业务编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 数量
     */
    private Integer qty;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 标签集合
     */
    private List<String> tags;
}
