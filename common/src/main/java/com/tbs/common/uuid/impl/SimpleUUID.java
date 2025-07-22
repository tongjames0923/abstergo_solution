package com.tbs.common.uuid.impl;

import cn.hutool.core.lang.UUID;
import com.tbs.common.util.encrypt.EncryptUitl;
import com.tbs.common.util.StrUtil;
import com.tbs.common.uuid.IUuidGenerator;

import java.nio.charset.StandardCharsets;

public class SimpleUUID implements IUuidGenerator {

    @Override
    public byte[] getUuid(int exceptLength) {
        int cnt = 0;
        byte[] result = new byte[exceptLength];
        while (cnt < exceptLength) {
            for (byte b : EncryptUitl.hash512Encrypt(UUID.fastUUID().toString()).getBytes(StandardCharsets.UTF_8)) {
                result[cnt++] = b;
                if (cnt >= exceptLength) {
                    return result;
                }
            }
        }
        return result;
    }

    @Override
    public String getUuidString(int exceptLength) {
        return StrUtil.bytesToHex(getUuid(exceptLength));
    }
}
