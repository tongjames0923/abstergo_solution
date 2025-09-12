package com.tbs.shared.interfaces.view.impl;

import com.alibaba.fastjson2.JSON;
import com.tbs.shared.constant.ViewConstants;
import com.tbs.shared.interfaces.view.IPropertyValueTransformer;
import org.springframework.stereotype.Component;

@Component(ViewConstants.VIEW_TRANSFORMER_TYPE_JSON)
public class JsonPropertyValueTransformer implements IPropertyValueTransformer {
    @Override
    public String transform(Object value) {
        return JSON.toJSONString(value);
    }

    @Override
    public <T> T reverseTransform(String value, Class<T> clazz) {
        return JSON.parseObject(value, clazz);
    }

}
