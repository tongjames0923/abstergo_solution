package com.tbs.shared.mapper.version;

import com.tbs.shared.pojo.entity.version.AppPackageEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface AppVersionPackageMapper extends IFrameworkMapper<AppPackageEntity, Long> {
}
