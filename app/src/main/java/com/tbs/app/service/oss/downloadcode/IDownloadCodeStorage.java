package com.tbs.app.service.oss.downloadcode;

/**
 * 下载码存储抽象接口
 * <p>
 * 定义下载码记录的持久化契约，隔离具体存储实现（当前为Redis实现，预留数据库扩展点）。
 * 新增存储实现只需实现本接口，不影响服务层与控制器调用方
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IDownloadCodeStorage {

    /**
     * 保存一条新的下载码记录
     *
     * @param info 下载码信息，code不允许为空
     */
    void save(OssDownloadCodeInfo info);

    /**
     * 更新已存在的下载码记录（整体覆盖）
     *
     * @param info 下载码信息，code对应的记录必须已存在
     * @return 记录存在且更新成功返回true，记录不存在返回false
     */
    boolean update(OssDownloadCodeInfo info);

    /**
     * 按唯一码查询下载码记录
     *
     * @param code 下载唯一码
     * @return 下载码信息，不存在时返回null
     */
    OssDownloadCodeInfo get(String code);

    /**
     * 按唯一码删除下载码记录
     *
     * @param code 下载唯一码
     * @return 记录存在且删除成功返回true，记录不存在返回false
     */
    boolean delete(String code);

    /**
     * 判断唯一码是否已存在
     *
     * @param code 下载唯一码
     * @return 存在返回true
     */
    boolean exists(String code);
}
