package com.tbs.shared.mapper.oss;

import com.tbs.shared.pojo.entity.oss.OssDownloadCodeEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * OSS公开下载码Mapper
 *
 * @author abstergo
 * @since 1.0.0
 */
@Repository
public interface OssDownloadCodeMapper extends IFrameworkMapper<OssDownloadCodeEntity, Long> {
}
