package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemLoginInfoEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 登录信息mapper
 *
 * @author abstergo
 */
@Repository
public interface LoginInfoMapper extends IFrameworkMapper<SystemLoginInfoEntity, Long> {


}
