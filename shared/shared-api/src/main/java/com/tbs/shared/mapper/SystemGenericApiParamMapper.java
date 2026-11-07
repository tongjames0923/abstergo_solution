package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 泛化API入参定义数据访问接口
 *
 * @author abstergo
 */
@Repository
public interface SystemGenericApiParamMapper extends IFrameworkMapper<SystemGenericApiParamEntity, Long> {

}
