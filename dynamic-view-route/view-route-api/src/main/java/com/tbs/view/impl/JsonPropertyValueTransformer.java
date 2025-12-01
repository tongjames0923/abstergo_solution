package com.tbs.view.impl;

import com.tbs.common.data.impl.JsonTransformer;
import com.tbs.view.IPropertyValueTransformer;
import com.tbs.view.constant.ViewConstants;
import org.springframework.stereotype.Component;

/**
 * JSON属性值转换器
 *
 * @author abstergo
 */
@Component(ViewConstants.VIEW_TRANSFORMER_TYPE_JSON)
public class JsonPropertyValueTransformer extends JsonTransformer<Object> implements IPropertyValueTransformer {
}
