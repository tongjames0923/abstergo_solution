package com.tbs.redis.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.IOException;

/**
 * 默认的Redis系统编解码器
 * <p>
 * 继承自Redisson的BaseCodec，将Spring RedisSerializer包装为Redisson可以使用的编解码器
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
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

    /**
     * 构造方法
     *
     * @param serializer Spring Redis序列化器
     */
    public DefaultSystemCodec(RedisSerializer serializer) {
        this.serializer = serializer;
    }

    /**
     * 获取值解码器
     * <p>
     * 返回用于解码Redis值的解码器
     * </p>
     *
     * @return 值解码器
     */
    @Override
    public Decoder<Object> getValueDecoder() {
        return decoder;
    }

    /**
     * 获取值编码器
     * <p>
     * 返回用于编码Redis值的编码器
     * </p>
     *
     * @return 值编码器
     */
    @Override
    public Encoder getValueEncoder() {
        return encoder;
    }
}
