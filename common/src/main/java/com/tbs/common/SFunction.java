package com.tbs.common;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 函数式接口
 *
 * @author tongj
 * @param <T>
 * @param <P>
 */
@FunctionalInterface
public interface SFunction<T, P> extends Function<T, P>, Serializable {
}
