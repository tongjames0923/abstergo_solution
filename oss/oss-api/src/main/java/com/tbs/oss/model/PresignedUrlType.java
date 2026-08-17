package com.tbs.oss.model;

/**
 * 预签名URL类型
 */
public enum PresignedUrlType {

    /** 下载（GET） */
    DOWNLOAD,

    /** 上传（PUT），用于客户端直传 */
    UPLOAD
}
