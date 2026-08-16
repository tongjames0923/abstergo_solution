package com.tbs.oss.minio.support;

import com.tbs.oss.core.ProgressListener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 进度统计输入流
 * <p>
 * MinIO SDK不直接支持传输进度回调，通过包装上传流统计已传输字节数， 驱动{@link ProgressListener}回调。为避免高频回调，按固定步长通知
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class ProgressInputStream extends FilterInputStream {

    /** 进度通知步长（64KiB） */
    private static final long NOTIFY_STEP = 64 * 1024L;

    /** 总字节数，未知时为 -1 */
    private final long totalBytes;

    /** 进度监听器 */
    private final ProgressListener listener;

    /** 已传输字节数 */
    private long transferred;

    /** 上次通知时的已传输字节数 */
    private long lastNotified;

    /**
     * 构建进度统计流
     *
     * @param in         被包装的输入流
     * @param totalBytes 总字节数，未知时传 -1
     * @param listener   进度监听器
     */
    public ProgressInputStream(InputStream in, long totalBytes, ProgressListener listener) {
        super(in);
        this.totalBytes = totalBytes;
        this.listener = listener;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            update(1);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            update(n);
        }
        return n;
    }

    /**
     * 更新传输进度并按步长通知监听器
     *
     * @param n 本次读取的字节数
     */
    private void update(long n) {
        transferred += n;
        boolean finished = totalBytes > 0 && transferred >= totalBytes;
        if (finished || transferred - lastNotified >= NOTIFY_STEP) {
            lastNotified = transferred;
            listener.onProgress(transferred, totalBytes);
        }
    }
}
