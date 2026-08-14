package com.tbs.common.data.impl;

import com.tbs.common.data.ITransformer;
import com.tbs.common.util.base.JsonUtils;

/**
 * Json转换器
 *
 * @param <T>
 * @author abstergo
 */
public class JsonTransformer<T> implements ITransformer<T> {
    @Override
    public byte[] transform(T value) {
        return JsonUtils.toJSONBytes(value);
    }

    @Override
    public <T1 extends T> T1 reverseTransform(byte[] value, Class<T1> clazz) {
        return JsonUtils.parseObject(value, clazz);
    }
}
