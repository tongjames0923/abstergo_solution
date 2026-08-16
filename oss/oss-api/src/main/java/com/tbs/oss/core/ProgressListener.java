package com.tbs.oss.core;

/**
 * 上传/下载进度监听器，由实现尽量支持；不支持进度的实现可忽略该回调
 */
public interface ProgressListener {

    /** 进度回调，transferredBytes 为已传输字节数，totalBytes 为总字节数（未知时为 -1） */
    void onProgress(long transferredBytes, long totalBytes);

    /** 传输成功完成 */
    void onSuccess();

    /** 传输失败 */
    void onError(Exception e);

    /**
     * 空实现适配器，只需覆写关心的回调
     */
    class Adapter implements ProgressListener {

        @Override
        public void onProgress(long transferredBytes, long totalBytes) {
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(Exception e) {
        }
    }
}
