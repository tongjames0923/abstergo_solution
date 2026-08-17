package com.tbs.app.service.version;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.version.AppPackageEntity;

import java.util.List;

/**
 * 应用安装包服务
 * <p>
 * 提供应用版本下安装包产物的登记与查询能力，安装包通过 versionId 挂接到应用版本，
 * 一个版本可登记多个安装包（多架构/多渠道）；文件实体的上传与下载由统一 OSS 门面承担，
 * 本服务仅持有存储坐标（桶名/对象名）或外部下载地址
 *
 * @author abstergo
 */
public interface IAppPackageService extends IServiceTemplate<AppPackageEntity, Long> {

    /**
     * 查询指定版本下的全部安装包
     *
     * @param versionId 应用版本ID
     * @return 该版本下的安装包列表，无数据时返回空集合
     */
    List<AppPackageEntity> listByVersionId(Long versionId);
}
