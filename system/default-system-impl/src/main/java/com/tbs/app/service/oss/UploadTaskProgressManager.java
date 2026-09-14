package com.tbs.app.service.oss;

import com.tbs.app.vo.UploadTaskProgressVO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 上传任务进度管理器
 * <p>
 * 以内存注册表维护上传任务写入对象存储的实时进度，供前端在上传请求执行期间轮询查询。
 * 进度条目在过期后自动清理，避免任务残留导致内存泄漏
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class UploadTaskProgressManager {

    /**
     * 进度条目保留时长，超期后清理（终态条目同样保留一段时间供前端查询最终结果）
     */
    private static final long EXPIRE_MILLIS = TimeUnit.MINUTES.toMillis(10);

    /**
     * 任务进度注册表：taskId -> 进度信息
     */
    private final Map<String, UploadTaskProgressVO> progressMap = new ConcurrentHashMap<>();

    /**
     * 注册上传任务，任务进入写入对象存储阶段
     *
     * @param taskId     上传任务ID
     * @param totalBytes 文件总字节数
     */
    public void register(String taskId, long totalBytes) {
        pruneExpired();
        UploadTaskProgressVO progress = new UploadTaskProgressVO();
        progress.setTaskId(taskId);
        progress.setStatus(UploadTaskProgressVO.STATUS_STORING);
        progress.setTotalBytes(totalBytes);
        progress.setTransferredBytes(0L);
        progress.setPercentage(0);
        progress.setUpdateTime(System.currentTimeMillis());
        progressMap.put(taskId, progress);
    }

    /**
     * 更新已写入字节数
     *
     * @param taskId           上传任务ID
     * @param transferredBytes 已写入字节数
     */
    public void updateTransferred(String taskId, long transferredBytes) {
        UploadTaskProgressVO progress = progressMap.get(taskId);
        if (progress == null) {
            return;
        }
        progress.setTransferredBytes(transferredBytes);
        progress.setPercentage(calcPercentage(transferredBytes, progress.getTotalBytes()));
        progress.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * 标记任务成功并置满进度
     *
     * @param taskId 上传任务ID
     */
    public void markSuccess(String taskId) {
        UploadTaskProgressVO progress = progressMap.get(taskId);
        if (progress == null) {
            return;
        }
        progress.setTransferredBytes(progress.getTotalBytes());
        progress.setPercentage(100);
        progress.setStatus(UploadTaskProgressVO.STATUS_SUCCESS);
        progress.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * 标记任务失败
     *
     * @param taskId 上传任务ID
     */
    public void markFailed(String taskId) {
        UploadTaskProgressVO progress = progressMap.get(taskId);
        if (progress == null) {
            return;
        }
        progress.setStatus(UploadTaskProgressVO.STATUS_FAILED);
        progress.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * 查询任务进度
     *
     * @param taskId 上传任务ID
     * @return 进度信息，任务不存在（仍在接收请求体或条目已过期）时返回null
     */
    public UploadTaskProgressVO get(String taskId) {
        return progressMap.get(taskId);
    }

    /**
     * 计算写入进度百分比
     *
     * @param transferredBytes 已写入字节数
     * @param totalBytes       文件总字节数
     * @return 进度百分比（0~100）
     */
    private int calcPercentage(long transferredBytes, long totalBytes) {
        if (totalBytes <= 0) {
            return 0;
        }
        return (int)Math.min(100L, transferredBytes * 100L / totalBytes);
    }

    /**
     * 清理过期进度条目
     */
    private void pruneExpired() {
        long deadline = System.currentTimeMillis() - EXPIRE_MILLIS;
        progressMap.entrySet().removeIf(entry -> entry.getValue().getUpdateTime() < deadline);
    }
}
