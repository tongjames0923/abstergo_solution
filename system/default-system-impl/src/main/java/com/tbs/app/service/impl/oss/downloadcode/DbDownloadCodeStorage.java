package com.tbs.app.service.impl.oss.downloadcode;

import com.tbs.common.util.base.StrUtil;
import com.tbs.oss.downloadcode.IDownloadCodeStorage;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import com.tbs.oss.downloadcode.OssDownloadCodeQuery;
import com.tbs.oss.downloadcode.OssDownloadCodeStatus;
import com.tbs.shared.enums.OssDownloadCodeFlagEnum;
import com.tbs.shared.mapper.oss.OssDownloadCodeMapper;
import com.tbs.shared.pojo.entity.oss.OssDownloadCodeEntity;
import com.tbs.sql.dsl.IQueryBuilder;
import com.tbs.sql.dsl.IWhereClause;
import com.tbs.sql.service.QueryEngine;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
     * 按条件分页查询下载码记录
     * <p>
     * 条件与状态筛选均下推至SQL：结果按创建时间倒序，limit小于等于0时不分页
     * </p>
     *
     * @param query 查询条件（含分页偏移）
     * @return 下载码记录列表
     */
    @Override
    public List<OssDownloadCodeInfo> list(OssDownloadCodeQuery query) {
        IQueryBuilder<OssDownloadCodeEntity> builder = buildQueryBuilder(query);
        if (query != null && query.getLimit() > 0) {
            int offset = Math.max(query.getOffset(), 0);
            builder.getPageLimitSupport().ifPresent(pageLimit -> pageLimit.pageOf(
                (long)(offset / query.getLimit()) + 1, (long)query.getLimit()));
        }
        builder.getOrderSupport().ifPresent(order -> order.orderByDesc(OssDownloadCodeEntity::getCreateTime));
        return builder.fetch().stream().map(this::toInfo).collect(Collectors.toList());
    }

    /**
     * 按条件统计下载码记录总数
     *
     * @param query 查询条件（分页参数被忽略）
     * @return 记录总数
     */
    @Override
    public long count(OssDownloadCodeQuery query) {
        return buildQueryBuilder(query).count().longValue();
    }

    /**
     * 构建带条件的查询构建器
     * <p>
     * code精确匹配、objectKey前缀匹配、keyword对码/对象键/备注做模糊匹配、
     * 状态翻译为过期时间戳比较条件
     * </p>
     *
     * @param query 查询条件（可为空）
     * @return 查询构建器
     */
    private IQueryBuilder<OssDownloadCodeEntity> buildQueryBuilder(OssDownloadCodeQuery query) {
        IWhereClause<OssDownloadCodeEntity> where = QueryEngine.getEntityQueryWrapper(OssDownloadCodeEntity.class)
            .query().where();
        if (query == null) {
            return where.pour();
        }
        if (!StrUtil.isEmpty(query.getCode())) {
            where.equal(OssDownloadCodeEntity::getCode, query.getCode());
        }
        if (!StrUtil.isEmpty(query.getObjectKey())) {
            where.leftLike(OssDownloadCodeEntity::getObjectKey, query.getObjectKey());
        }
        if (!StrUtil.isEmpty(query.getKeyword())) {
            String keyword = query.getKeyword();
            where.andInclude(sub -> sub
                .like(OssDownloadCodeEntity::getCode, keyword)
                .or().like(OssDownloadCodeEntity::getObjectKey, keyword)
                .or().like(OssDownloadCodeEntity::getRemark, keyword));
        }
        applyStatusCondition(where, query.getStatus());
        return where.pour();
    }

    /**
     * 将状态条件翻译为过期时间戳比较条件
     *
     * @param where  条件构建器
     * @param status 状态筛选（可为空）
     */
    private void applyStatusCondition(IWhereClause<OssDownloadCodeEntity> where, OssDownloadCodeStatus status) {
        if (status == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (OssDownloadCodeStatus.PERMANENT == status) {
            where.lessThanOrEqual(OssDownloadCodeEntity::getExpireAt, OssDownloadCodeInfo.NEVER_EXPIRE);
        } else if (OssDownloadCodeStatus.EXPIRED == status) {
            where.greaterThan(OssDownloadCodeEntity::getExpireAt, OssDownloadCodeInfo.NEVER_EXPIRE)
                .and().lessThanOrEqual(OssDownloadCodeEntity::getExpireAt, now);
        } else {
            where.andInclude(sub -> sub
                .greaterThan(OssDownloadCodeEntity::getExpireAt, now)
                .or().lessThanOrEqual(OssDownloadCodeEntity::getExpireAt, OssDownloadCodeInfo.NEVER_EXPIRE));
        }
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
