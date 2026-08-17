package com.tbs.shared.mapper.version;

import com.tbs.shared.pojo.entity.version.AppVersionEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 应用版本Mapper
 *
 * @author abstergo
 */
@Repository
public interface AppVersionMapper extends IFrameworkMapper<AppVersionEntity, Long> {
}
