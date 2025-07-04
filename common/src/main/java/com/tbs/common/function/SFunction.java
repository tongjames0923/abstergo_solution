package com.tbs.common.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 序列化的Function
 *
 * @param <T>
 * @param <P>
 * @author tongj
 */
@FunctionalInterface
public interface SFunction<T, P> extends Function<T, P>, Serializable {
}
