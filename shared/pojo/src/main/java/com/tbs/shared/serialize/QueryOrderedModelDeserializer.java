package com.tbs.shared.serialize;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tbs.sql.enums.QueryOrderEnum;
import com.tbs.sql.model.QueryOrderedModel;

/**
 * QueryOrderedModel 反序列化器
 */
public class QueryOrderedModelDeserializer extends JsonDeserializer<QueryOrderedModel> {
	@Override
	public QueryOrderedModel deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getValueAsString();
		if (value != null) {
			QueryOrderEnum order = QueryOrderEnum.getOrder(value);
			return new QueryOrderedModel(order);
		}
		return null;
	}
}
