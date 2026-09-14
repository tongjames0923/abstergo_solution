package com.tbs.redis.oss;

import com.tbs.common.util.base.StrUtil;
import com.tbs.oss.downloadcode.IDownloadCodeStorage;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import com.tbs.oss.downloadcode.OssDownloadCodeQuery;
import com.tbs.oss.downloadcode.OssDownloadCodeStatus;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 下载码Redis存储实现
 * <p>
 * 基于Redisson的{@link RBucket}持久化下载码记录，键前缀统一为{@code tbs:oss:download-code:}。
 * 过期策略：记录内expireAt字段与Redis TTL双保险，TTL到期由Redis自动清理；
 * 永不过期（expireAt小于等于{@link OssDownloadCodeInfo#NEVER_EXPIRE}）时不设置TTL。
 * 由 {@code com.tbs.redis.oss.OssDownloadCodeStorageConfig} 按
 * {@code tbs.oss.download-code.storage=redis}（或缺省）条件装配
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class RedisDownloadCodeStorage implements IDownloadCodeStorage {

    /**
     * 下载码记录的Redis键前缀
     */
    private static final String KEY_PREFIX = "tbs:oss:download-code:";

    /**
     * Redisson客户端实例
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * 保存一条新的下载码记录
     * <p>
     * 有过期时间时同时写入Redis TTL，由Redis到期自动清理
     * </p>
     *
     * @param info 下载码信息，code不允许为空
     */
    @Override
    public void save(OssDownloadCodeInfo info) {
        RBucket<OssDownloadCodeInfo> bucket = redissonClient.getBucket(KEY_PREFIX + info.getCode());
        bucket.set(info);
        applyExpire(bucket, info.getExpireAt());
    }

    /**
     * 更新已存在的下载码记录（整体覆盖并重新应用TTL）
     *
     * @param info 下载码信息，code对应的记录必须已存在
     * @return 记录存在且更新成功返回true，记录不存在返回false
     */
    @Override
    public boolean update(OssDownloadCodeInfo info) {
        RBucket<OssDownloadCodeInfo> bucket = redissonClient.getBucket(KEY_PREFIX + info.getCode());
        if (!bucket.isExists()) {
            return false;
        }
        bucket.set(info);
        applyExpire(bucket, info.getExpireAt());
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
        return redissonClient.<OssDownloadCodeInfo>getBucket(KEY_PREFIX + code).get();
    }

    /**
     * 按唯一码删除下载码记录
     *
     * @param code 下载唯一码
     * @return 记录存在且删除成功返回true，记录不存在返回false
     */
    @Override
    public boolean delete(String code) {
        return redissonClient.getBucket(KEY_PREFIX + code).delete();
    }

    /**
     * 判断唯一码是否已存在
     *
     * @param code 下载唯一码
     * @return 存在返回true
     */
    @Override
    public boolean exists(String code) {
        return redissonClient.getBucket(KEY_PREFIX + code).isExists();
    }

    /**
     * 按条件分页查询下载码记录
     * <p>
     * Redis无原生条件查询能力，采用SCAN遍历键后内存过滤、按创建时间倒序再分页；
     * 数据量级较大时开销随键数量增长，管理界面场景下可接受，必要时可切换数据库存储
     * </p>
     *
     * @param query 查询条件（含分页偏移）
     * @return 下载码记录列表
     */
    @Override
    public List<OssDownloadCodeInfo> list(OssDownloadCodeQuery query) {
        List<OssDownloadCodeInfo> all = scanMatched(query);
        if (query == null || query.getLimit() <= 0) {
            return all;
        }
        int from = Math.min(Math.max(query.getOffset(), 0), all.size());
        int to = Math.min(from + query.getLimit(), all.size());
        return new ArrayList<>(all.subList(from, to));
    }

    /**
     * 按条件统计下载码记录总数
     *
     * @param query 查询条件（分页参数被忽略）
     * @return 记录总数
     */
    @Override
    public long count(OssDownloadCodeQuery query) {
        return scanMatched(query).size();
    }

    /**
     * 扫描全部下载码键并按条件过滤
     *
     * @param query 查询条件（可为空）
     * @return 匹配记录（按创建时间倒序）
     */
    private List<OssDownloadCodeInfo> scanMatched(OssDownloadCodeQuery query) {
        List<OssDownloadCodeInfo> matched = new ArrayList<>();
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(KEY_PREFIX + "*");
        for (String key : keys) {
            OssDownloadCodeInfo info = redissonClient.<OssDownloadCodeInfo>getBucket(key).get();
            if (info != null && isMatched(info, query)) {
                matched.add(info);
            }
        }
        matched.sort(Comparator.comparingLong(OssDownloadCodeInfo::getCreateTime).reversed());
        return matched;
    }

    /**
     * 判断记录是否满足查询条件
     *
     * @param info  下载码信息
     * @param query 查询条件（可为空）
     * @return 满足返回true
     */
    private boolean isMatched(OssDownloadCodeInfo info, OssDownloadCodeQuery query) {
        if (query == null) {
            return true;
        }
        if (!StrUtil.isEmpty(query.getCode()) && !query.getCode().equals(info.getCode())) {
            return false;
        }
        if (!StrUtil.isEmpty(query.getObjectKey()) && (info.getObjectKey() == null
            || !info.getObjectKey().startsWith(query.getObjectKey()))) {
            return false;
        }
        if (!StrUtil.isEmpty(query.getKeyword()) && !containsKeyword(info, query.getKeyword())) {
            return false;
        }
        return isStatusMatched(info, query.getStatus());
    }

    /**
     * 判断关键字是否命中码/对象键/备注
     *
     * @param info    下载码信息
     * @param keyword 关键字
     * @return 命中返回true
     */
    private boolean containsKeyword(OssDownloadCodeInfo info, String keyword) {
        return (info.getCode() != null && info.getCode().contains(keyword))
            || (info.getObjectKey() != null && info.getObjectKey().contains(keyword))
            || (info.getRemark() != null && info.getRemark().contains(keyword));
    }

    /**
     * 判断记录状态是否匹配筛选条件
     *
     * @param info   下载码信息
     * @param status 状态筛选（可为空）
     * @return 匹配返回true
     */
    private boolean isStatusMatched(OssDownloadCodeInfo info, OssDownloadCodeStatus status) {
        if (status == null) {
            return true;
        }
        boolean permanent = info.getExpireAt() <= OssDownloadCodeInfo.NEVER_EXPIRE;
        if (OssDownloadCodeStatus.PERMANENT == status) {
            return permanent;
        }
        if (OssDownloadCodeStatus.EXPIRED == status) {
            return !permanent && info.isExpired(System.currentTimeMillis());
        }
        return permanent || !info.isExpired(System.currentTimeMillis());
    }

    /**
     * 根据记录内的过期时间戳应用Redis TTL
     * <p>
     * 永不过期时不设置TTL；过期时间已超过当前时间时TTL写入立即失败，
     * 由服务层在保存前完成过期语义处理
     * </p>
     *
     * @param bucket   目标桶
     * @param expireAt 过期时间戳（毫秒），小于等于0表示永不过期
     */
    private void applyExpire(RBucket<OssDownloadCodeInfo> bucket, long expireAt) {
        if (expireAt > OssDownloadCodeInfo.NEVER_EXPIRE) {
            bucket.expireAt(expireAt);
        }
    }
}
