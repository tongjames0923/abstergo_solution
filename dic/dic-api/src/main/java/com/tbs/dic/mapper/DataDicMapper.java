package com.tbs.dic.mapper;

import com.tbs.dic.entity.SystemDataDicEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataDicMapper extends IFrameworkMapper<SystemDataDicEntity, Long> {
}
