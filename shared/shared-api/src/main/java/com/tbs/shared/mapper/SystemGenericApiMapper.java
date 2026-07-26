package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 泛化API配置数据访问接口
 *
 * @author abstergo
 */
@Repository
public interface SystemGenericApiMapper extends IFrameworkMapper<SystemGenericApiEntity, Long> {

}
