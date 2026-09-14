package com.tbs.app.service.impl.oss.downloadcode;

import com.tbs.oss.downloadcode.IDownloadCodeStorage;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import com.tbs.shared.enums.OssDownloadCodeFlagEnum;
import com.tbs.shared.mapper.oss.OssDownloadCodeMapper;
import com.tbs.shared.pojo.entity.oss.OssDownloadCodeEntity;
import com.tbs.sql.service.QueryEngine;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 下载码数据库存储实现
 * <p>
 * 基于Mapper与QueryEngine持久化下载码记录到表oss_download_code；
 * 删除语义为flag逻辑删除，已删除记录由{@code SqlLogicDeleteIntercepter}自动过滤。
 * 由 {@code com.tbs.app.config.OssDownloadCodeStorageConfig} 按
 * {@code tbs.oss.download-code.storage=db} 条件装配
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class DbDownloadCodeStorage implements IDownloadCodeStorage {

    /**
     * 下载码Mapper
     */
    @Resource
    private OssDownloadCodeMapper ossDownloadCodeMapper;

    /**
     * 保存一条新的下载码记录
     *
     * @param info 下载码信息，code不允许为空
     */
    @Override
    public void save(OssDownloadCodeInfo info) {
        ossDownloadCodeMapper.insert(toEntity(info));
    }

    /**
     * 更新已存在的下载码记录（整体覆盖）
     *
     * @param info 下载码信息，code对应的记录必须已存在
     * @return 记录存在且更新成功返回true，记录不存在返回false
     */
    @Override
    public boolean update(OssDownloadCodeInfo info) {
        OssDownloadCodeEntity entity = fetchEntity(info.getCode());
        if (entity == null) {
            return false;
        }
        entity.setBucket(info.getBucket());
        entity.setObjectKey(info.getObjectKey());
        entity.setExpireAt(info.getExpireAt());
        entity.setRemark(info.getRemark());
        entity.setUpdateTime(new Date());
        ossDownloadCodeMapper.updateByPrimaryKey(entity);
        return true;
    }

    /**
     * 按唯一码查询下载码记录
     *
     * @param code 下载唯一码
     * @return 下载码信息，不存在时返回null
     */
    @Override
    public OssDownloadCodeInfo get(String code) {
        OssDownloadCodeEntity entity = fetchEntity(code);
        return entity == null ? null : toInfo(entity);
    }

    /**
     * 按唯一码逻辑删除下载码记录（置flag为已删除）
     *
     * @param code 下载唯一码
     * @return 记录存在且删除成功返回true，记录不存在返回false
     */
    @Override
    public boolean delete(String code) {
        OssDownloadCodeEntity entity = fetchEntity(code);
        if (entity == null) {
            return false;
        }
        entity.setFlag(OssDownloadCodeFlagEnum.DELETED.getCode());
        entity.setUpdateTime(new Date());
        ossDownloadCodeMapper.updateByPrimaryKey(entity);
        return true;
    }

    /**
     * 判断唯一码是否已存在
     *
     * @param code 下载唯一码
     * @return 存在返回true
     */
    @Override
    public boolean exists(String code) {
        return fetchEntity(code) != null;
    }

    /**
     * 按唯一码查询有效记录实体
     * <p>
     * 已逻辑删除的记录由拦截器自动过滤，查询不到即视为不存在
     * </p>
     *
     * @param code 下载唯一码
     * @return 有效记录实体，不存在时返回null
     */
    private OssDownloadCodeEntity fetchEntity(String code) {
        return QueryEngine.getEntityQueryWrapper(OssDownloadCodeEntity.class).query().where()
            .equal(OssDownloadCodeEntity::getCode, code).pour().fetchOne();
    }

    /**
     * 契约模型转实体
     *
     * @param info 下载码信息
     * @return 实体对象
     */
    private OssDownloadCodeEntity toEntity(OssDownloadCodeInfo info) {
        OssDownloadCodeEntity entity = new OssDownloadCodeEntity();
        entity.setCode(info.getCode());
        entity.setBucket(info.getBucket());
        entity.setObjectKey(info.getObjectKey());
        entity.setExpireAt(info.getExpireAt());
        entity.setRemark(info.getRemark());
        entity.setCreateTime(new Date(info.getCreateTime()));
        entity.setFlag(OssDownloadCodeFlagEnum.ACTIVE.getCode());
        return entity;
    }

    /**
     * 实体转契约模型
     *
     * @param entity 实体对象
     * @return 下载码信息
     */
    private OssDownloadCodeInfo toInfo(OssDownloadCodeEntity entity) {
        OssDownloadCodeInfo info = new OssDownloadCodeInfo();
        info.setCode(entity.getCode());
        info.setBucket(entity.getBucket());
        info.setObjectKey(entity.getObjectKey());
        info.setExpireAt(entity.getExpireAt() == null ? OssDownloadCodeInfo.NEVER_EXPIRE : entity.getExpireAt());
        info.setRemark(entity.getRemark());
        info.setCreateTime(entity.getCreateTime() == null ? System.currentTimeMillis() : entity.getCreateTime().getTime());
        return info;
    }
}
