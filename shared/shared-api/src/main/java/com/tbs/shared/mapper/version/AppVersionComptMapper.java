package com.tbs.shared.mapper.version;

import com.tbs.shared.pojo.entity.version.AppVersionCompatibilityPolicyEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface AppVersionComptMapper extends IFrameworkMapper<AppVersionCompatibilityPolicyEntity, Long> {
}
