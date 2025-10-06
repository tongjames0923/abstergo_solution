package com.tbs.redis.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;

public class DefaultSystemCodec extends BaseCodec {
    RedisSerializer serializer;

    private Decoder<Object> decoder = (buf, state) -> {
        // 正确处理直接缓冲区的情况
        byte[] bytes;
        if (buf.hasArray()) {
            bytes = buf.array();
        } else {
            bytes = ByteBufUtil.getBytes(buf);
        }
        return serializer.deserialize(bytes);
    };
    
    private Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object o) throws IOException {
            byte[] bytes = serializer.serialize(o);
            ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(bytes.length);
            buf.writeBytes(bytes);
            return buf;
        }
    };

    public DefaultSystemCodec(RedisSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
}