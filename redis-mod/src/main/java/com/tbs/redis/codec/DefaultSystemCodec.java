package com.tbs.redis.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;

public class DefaultSystemCodec extends BaseCodec {
    RedisSerializer serializer;

    private Decoder<Object> decoder = (buf, state) -> {
        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        return serializer.deserialize(result);
    };

    private Encoder encoder = new Encoder() {
        @Override
        public ByteBuf encode(Object o) throws IOException {
            return Unpooled.wrappedBuffer(serializer.serialize(o));
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
