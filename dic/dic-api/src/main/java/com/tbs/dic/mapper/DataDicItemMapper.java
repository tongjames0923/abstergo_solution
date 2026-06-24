package com.tbs.dic.mapper;

import com.tbs.dic.entity.SystemDataDicItemEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataDicItemMapper extends IFrameworkMapper<SystemDataDicItemEntity, Long> {
}
