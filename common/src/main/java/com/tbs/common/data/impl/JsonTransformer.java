package com.tbs.common.data.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.data.ITransformer;

/**
 * Json转换器
 *
 * @param <T>
 * @author abstergo
 */
public class JsonTransformer<T> implements ITransformer<T> {
    @Override
    public byte[] transform(T value) {
        return JSON.toJSONBytes(value);
    }

    @Override
    public <T1 extends T> T1 reverseTransform(byte[] value, Class<T1> clazz) {
        return JSON.parseObject(value, clazz);
    }
}
