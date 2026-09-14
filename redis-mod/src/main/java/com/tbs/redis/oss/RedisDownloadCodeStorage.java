package com.tbs.redis.oss;

import com.tbs.oss.downloadcode.IDownloadCodeStorage;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;

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
