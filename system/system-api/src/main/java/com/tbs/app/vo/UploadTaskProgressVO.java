package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 上传任务进度VO
 * <p>
 * 用于前端在上传请求执行期间轮询服务端写入对象存储的实时进度。
 * 状态取值含义：RECEIVING-网关仍在接收请求体（任务未注册）；
 * STORING-正在写入对象存储；SUCCESS-上传成功；FAILED-上传失败
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class UploadTaskProgressVO implements Serializable {

    /** 网关仍在接收请求体，任务尚未进入写入阶段 */
    public static final String STATUS_RECEIVING = "RECEIVING";

    /** 正在写入对象存储 */
    public static final String STATUS_STORING = "STORING";

    /** 上传成功 */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /** 上传失败 */
    public static final String STATUS_FAILED = "FAILED";

    /**
     * 上传任务ID
     */
    private String taskId;

    /**
     * 任务状态（见STATUS_*常量）
     */
    private String status;

    /**
     * 文件总字节数
     */
    private long totalBytes;

    /**
     * 已写入字节数
     */
    private long transferredBytes;

    /**
     * 写入进度百分比（0~100）
     */
    private int percentage;

    /**
     * 最近更新时间戳（毫秒）
     */
    private long updateTime;
}
