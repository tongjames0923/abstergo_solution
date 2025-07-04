package com.tbs.common.function;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * 序列化的BiConsumer
 *
 * @param <T>
 * @param <P>
 * @author tongj
 */
public interface SBiConsumer<T, P> extends BiConsumer<T, P>, Serializable {
}
