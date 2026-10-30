package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据处理器校验结果视图对象
 * <p>
 * 用于前端"可选可输入"模式下的自定义 handlerId 实时校验：
 * {@code available} 表示该 ID 是否可被解析为可用处理器，
 * {@code source} 表示命中来源（local 本地 Bean / remote Dubbo 远程实例），
 * 不可用时为 {@code null}。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandlerValidateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 校验结果：true 表示处理器可用
     */
    private boolean available;

    /**
     * 命中来源：local 表示本地 Bean，remote 表示 Dubbo 远程实例，不可用时为 null
     */
    private String source;
}
