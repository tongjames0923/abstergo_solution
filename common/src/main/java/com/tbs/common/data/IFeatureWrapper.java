package com.tbs.common.data;

import com.tbs.common.util.helper.FeatureWrapperHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 功能包装接口
 * <p>
 * 该接口定义了一个通用的包装器模式，用于包装和管理特定类型的对象。 通过该接口可以获取和设置被包装的原始对象，实现对对象的间接访问和控制。
 *
 * @param <T> 被包装对象的类型
 * @author abstergo
 */
public interface IFeatureWrapper<T> {
	/**
	 * 获取包装的原始对象
	 * <p>
	 * 返回当前包装器中存储的原始对象实例。
	 *
	 * @return 被包装的原始对象，可能为null
	 */
	T getOriginal();

	/**
	 * 设置包装的原始对象
	 * <p>
	 * 更新当前包装器中存储的原始对象实例。
	 *
	 * @param orginal 要设置的原始对象，可以为null
	 */
	void setOriginal(T orginal);

    /**
     * 获取包装的指定功能对象
     * <p>
     * 返回当前包装器中存储的指定功能对象实例。
     *
     * @param featureClass 功能对象类型
     * @return 指定功能对象，可能为null
     */
    default <F> Optional<F> getFeatureIfExsit( Class<F> featureClass) {
        return FeatureWrapperHelper.unwrap(this, featureClass);
    }

}
