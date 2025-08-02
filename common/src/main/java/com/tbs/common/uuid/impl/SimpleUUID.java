package com.tbs.common.uuid.impl;

import com.tbs.common.uuid.IUuidGenerator;

import java.util.Random;

/**
 * @author abstergo
 */
public class SimpleUUID implements IUuidGenerator {

    Random random = new Random(System.currentTimeMillis());

    @Override
    public byte[] getUuid(int exceptLength) {
        return getUuidString(exceptLength).getBytes();
    }

    @Override
    public String getUuidString(int exceptLength) {
        long uuid = 0L;
        StringBuilder stringBuilder = new StringBuilder();
        while (stringBuilder.length() < exceptLength) {
            uuid = random.nextLong();
            stringBuilder.append(Long.toHexString(uuid));
        }
        return stringBuilder.substring(0, exceptLength);
    }
}
