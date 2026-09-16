package com.tbs.shared.mapper;

import org.springframework.stereotype.Repository;

import com.tbs.shared.pojo.entity.SystemUserExternalIdentityEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

/**
 * 系统用户外部身份绑定 Mapper
 *
 * @author abstergo
 */
@Repository
public interface SystemUserExternalIdentityMapper extends IFrameworkMapper<SystemUserExternalIdentityEntity, Long> {
}
