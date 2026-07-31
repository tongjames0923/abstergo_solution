package com.tbs.shared.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.tbs.sql.enums.OrderEnum;
import com.tbs.sql.order.impl.OrderedModel;

/**
 * OrderedModel 反序列化器
 */
public class OrderedModelDeserializer extends JsonDeserializer<OrderedModel> {
	@Override
	public OrderedModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		// 尝试作为字符串解析（如 "DESC"）
		String value = p.getValueAsString();
		if (value != null && !value.isEmpty()) {
			OrderEnum order = OrderEnum.getOrder(value);
			return new OrderedModel(order);
		}

		// 尝试作为对象解析（如 {"ordered": "DESC"} 或 {"order": "DESC"}）
		JsonNode node = p.getCodec().readTree(p);
		if (node != null && !node.isNull()) {
			String orderStr = null;
			if (node.has("ordered")) {
				orderStr = node.get("ordered").asText();
			} else if (node.has("order")) {
				orderStr = node.get("order").asText();
			}
			if (orderStr != null) {
				OrderEnum order = OrderEnum.getOrder(orderStr);
				return new OrderedModel(order);
			}
		}
		return null;
	}
}
