package com.tbs.app.service.oss.downloadcode;

import com.tbs.app.form.OssDownloadCodeSearchForm;
import com.tbs.app.vo.OssDownloadCodeStatisticsVO;
import com.tbs.common.data.PageVO;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;

/**
 * OSS公开下载码服务契约
 * <p>
 * 提供下载码的生成、增删改查与下载前解析能力。
 * 控制器仅依赖本契约接口，具体实现见实现层；
 * 下载码信息模型与存储抽象契约见 oss-api 的 com.tbs.oss.downloadcode 包
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface IOssDownloadCodeService {

    /**
     * 为指定OSS对象生成公开下载唯一码
     * <p>
     * code为空时生成16位Base64字符随机码，冲突时自动重试；
     * code非空时使用自定义码，须为4~64位Base64字符且全局唯一，否则报用户输入错误；
     * 创建前校验对象在存储桶中真实存在，不存在时报用户输入错误；
     * expireSeconds为空或小于等于0时表示永不过期
     * </p>
     *
     * @param code          下载唯一码（可选，空则自动生成；非空须为4~64位Base64字符且唯一）
     * @param objectKey     存储对象键（必填）
     * @param bucket        存储桶名（可选，缺省使用默认桶）
     * @param expireSeconds 有效期秒数（可选，空或小于等于0表示永不过期）
     * @param remark        备注说明（可选）
     * @return 创建成功的下载码信息
     */
    OssDownloadCodeInfo createCode(String code, String objectKey, String bucket, Long expireSeconds, String remark);

    /**
     * 按唯一码查询下载码信息
     *
     * @param code 下载唯一码（必填）
     * @return 下载码信息
     */
    OssDownloadCodeInfo getCode(String code);

    /**
     * 更新下载码信息
     * <p>
     * 支持续期（重新指定有效期）与修改备注；expireSeconds为空表示不修改有效期，
     * 显式传入小于等于0表示改为永不过期
     * </p>
     *
     * @param code          下载唯一码（必填）
     * @param expireSeconds 有效期秒数（可选，null表示不修改；小于等于0表示永不过期）
     * @param remark        备注说明（可选，null表示不修改）
     */
    void updateCode(String code, Long expireSeconds, String remark);

    /**
     * 删除下载码
     *
     * @param code 下载唯一码（必填）
     */
    void deleteCode(String code);

    /**
     * 解析用于公开下载的唯一码
     * <p>
     * 校验唯一码存在且未过期，返回其对应的下载码信息；
     * 码无效或已过期时报用户级错误，且不泄露存储细节
     * </p>
     *
     * @param code 下载唯一码（必填）
     * @return 校验通过的下载码信息
     */
    OssDownloadCodeInfo resolveForDownload(String code);

    /**
     * 分页查询下载码
     * <p>
     * 支持按码精确匹配、对象键前缀、关键字（码/对象键/备注）与状态筛选，结果按创建时间倒序；
     * 分页参数非法时归一化为默认值（页码1、每页10、上限100），状态取值非法时报用户输入错误
     * </p>
     *
     * @param form     查询条件表单（可为空，为空表示不限条件）
     * @param pageNum  页码（可选，从1开始，缺省1）
     * @param pageSize 每页条数（可选，缺省10，上限100）
     * @return 分页结果（总数与当前页数据）
     */
    PageVO<OssDownloadCodeInfo> queryPage(OssDownloadCodeSearchForm form, Long pageNum, Long pageSize);

    /**
     * 统计下载码概览
     *
     * @return 统计结果（总数/有效/已过期/永久有效）
     */
    OssDownloadCodeStatisticsVO statistics();
}
