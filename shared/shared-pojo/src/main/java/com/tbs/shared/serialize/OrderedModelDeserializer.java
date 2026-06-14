package com.tbs.shared.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.impl.OrderedModel;

/**
 * OrderedModel 反序列化器
 */
public class OrderedModelDeserializer extends JsonDeserializer<OrderedModel> {
	@Override
	public OrderedModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getValueAsString();
		if (value != null) {
			OrderEnum order = OrderEnum.getOrder(value);
			return new OrderedModel(order);
		}
		return null;
	}
}
