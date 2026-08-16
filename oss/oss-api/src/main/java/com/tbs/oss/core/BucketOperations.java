package com.tbs.oss.core;

import com.tbs.oss.model.Bucket;

import java.util.List;

/**
 * 桶级操作契约
 */
public interface BucketOperations {

    /** 创建桶 */
    void createBucket(String bucketName);

    /** 删除桶（桶非空时由实现抛出异常） */
    void deleteBucket(String bucketName);

    /** 判断桶是否存在 */
    boolean doesBucketExist(String bucketName);

    /** 列出所有桶 */
    List<Bucket> listBuckets();
}
