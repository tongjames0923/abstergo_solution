package com.tbs.shared.interfaces.view.impl;

import com.tbs.common.data.impl.JsonTransformer;
import com.tbs.shared.constant.ViewConstants;
import com.tbs.shared.interfaces.view.IPropertyValueTransformer;
import org.springframework.stereotype.Component;

/**
 * JSON属性值转换器
 *
 * @author abstergo
 */
@Component(ViewConstants.VIEW_TRANSFORMER_TYPE_JSON)
public class JsonPropertyValueTransformer extends JsonTransformer<Object> implements IPropertyValueTransformer {
}
