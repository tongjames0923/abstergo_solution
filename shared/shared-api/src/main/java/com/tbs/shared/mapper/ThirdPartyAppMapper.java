package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 第三方应用数据访问映射器。
 * <p>基于框架通用 Mapper {@code IFrameworkMapper} 提供的 CRUD 与条件查询能力，无需额外编写 SQL。</p>
 */
@Mapper
public interface ThirdPartyAppMapper extends IFrameworkMapper<ThirdPartyAppEntity, Long> {
}
