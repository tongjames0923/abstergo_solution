package com.tbs.common;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * 自定义BiConsumer
 *
 * @author tongj
 * @param <T>
 * @param <P>
 */
public interface SBiConsumer<T, P> extends BiConsumer<T, P>, Serializable {
}
