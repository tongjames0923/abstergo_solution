package com.tbs.shared.mapper;

import org.springframework.stereotype.Repository;

import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Repository
public interface SystemCompanyMapper extends IFrameworkMapper<SystemCompanyEntity, Long> {
}
