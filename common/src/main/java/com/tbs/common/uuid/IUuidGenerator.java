package com.tbs.common.uuid;

/**
 * UUID生成器
 *
 * @author abstergo
 */
public interface IUuidGenerator {
    /**
     * 获取UUID字节数组
     *
     * @param exceptLength 期望长度
     * @return
     */
    byte[] getUuid(int exceptLength);

    /**
     * 获取UUID字符串
     *
     * @param exceptLength 期望长度
     * @return
     */
    String getUuidString(int exceptLength);
}
