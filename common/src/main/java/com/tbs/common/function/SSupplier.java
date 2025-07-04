package com.tbs.common.function;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 序列化的Supplier
 *
 * @param <T>
 */
@FunctionalInterface
public interface SSupplier<T> extends Supplier<Object>, Serializable {
}
