package com.tbs.shared.mapper.view;

import com.tbs.shared.pojo.entity.view.PropertyValueEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

import java.util.List;

public interface PropertyValueMapper extends IFrameworkMapper<PropertyValueEntity, Long> {

    List<PropertyValueEntity> findByTypeAndRelationId(Integer type, Long relationId);

}
