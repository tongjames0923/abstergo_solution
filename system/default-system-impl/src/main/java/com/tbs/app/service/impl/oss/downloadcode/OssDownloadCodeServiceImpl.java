package com.tbs.app.service.impl.oss.downloadcode;

import com.tbs.app.constant.FileProcessI18nConstant;
import com.tbs.app.form.OssDownloadCodeSearchForm;
import com.tbs.app.service.oss.downloadcode.IOssDownloadCodeService;
import com.tbs.app.vo.OssDownloadCodeStatisticsVO;
import com.tbs.common.data.PageVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.oss.core.OssClient;
import com.tbs.oss.downloadcode.IDownloadCodeStorage;
import com.tbs.oss.downloadcode.OssDownloadCodeInfo;
import com.tbs.oss.downloadcode.OssDownloadCodeQuery;
import com.tbs.oss.downloadcode.OssDownloadCodeStatus;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * OSS公开下载码服务实现
 * <p>
 * 负责下载码的生成（16位Base64随机码，冲突自动重试）、对象存在性校验、
 * 增删改查与下载前解析校验；对OSS与存储层的依赖收敛在本实现内
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Service
public class OssDownloadCodeServiceImpl implements IOssDownloadCodeService {

    /**
     * 下载码冲突时的最大重试次数
     */
    private static final int MAX_GENERATE_ATTEMPTS = 5;

    /**
     * 生成16字符Base64码所需的随机字节数（12字节编码后恰好16字符）
     */
    private static final int RANDOM_BYTES = 12;

    /**
     * 自定义下载码合法格式：4~64位URL安全Base64字符（字母、数字、-、_）
     */
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{4,64}$");

    /**
     * 默认页码
     */
    private static final long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页条数
     */
    private static final long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 每页条数上限
     */
    private static final long MAX_PAGE_SIZE = 100L;

    /**
     * 下载码存储实现
     */
    @Resource
    private IDownloadCodeStorage downloadCodeStorage;

    /**
     * OSS统一门面
     */
    @Resource
    private OssClient ossClient;

    /**
     * 为指定OSS对象生成公开下载唯一码
     * <p>
     * 校验对象存在后落库：code为空时自动生成16位Base64随机码（冲突有限次重试），
     * code非空时使用自定义码，须通过格式与唯一性校验
     * </p>
     *
     * @param code          下载唯一码（可选，空则自动生成；非空须为4~64位Base64字符且唯一）
     * @param objectKey     存储对象键（必填）
     * @param bucket        存储桶名（可选，缺省使用默认桶）
     * @param expireSeconds 有效期秒数（可选，空或小于等于0表示永不过期）
     * @param remark        备注说明（可选）
     * @return 创建成功的下载码信息
     */
    @Override
    public OssDownloadCodeInfo createCode(String code, String objectKey, String bucket, Long expireSeconds,
        String remark) {
        assertObjectKeyNotEmpty(objectKey);
        String resolvedBucket = resolveBucket(bucket);
        AssertUtil.isTrue(!ossClient.doesObjectExist(resolvedBucket, objectKey))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_OBJECT_NOT_EXIST, objectKey)
            .throwIt();
        long now = System.currentTimeMillis();
        OssDownloadCodeInfo info = new OssDownloadCodeInfo();
        info.setCode(resolveCreateCode(code));
        info.setBucket(resolvedBucket);
        info.setObjectKey(objectKey);
        info.setExpireAt(resolveExpireAt(expireSeconds, now));
        info.setCreateTime(now);
        info.setRemark(remark);
        downloadCodeStorage.save(info);
        return info;
    }

    /**
     * 按唯一码查询下载码信息
     *
     * @param code 下载唯一码（必填）
     * @return 下载码信息
     */
    @Override
    public OssDownloadCodeInfo getCode(String code) {
        assertCodeNotEmpty(code);
        OssDownloadCodeInfo info = downloadCodeStorage.get(code);
        assertCodeFound(info, code);
        return info;
    }

    /**
     * 更新下载码信息
     * <p>
     * expireSeconds为null表示不修改有效期，显式传入小于等于0表示改为永不过期；
     * remark为null表示不修改备注
     * </p>
     *
     * @param code          下载唯一码（必填）
     * @param expireSeconds 有效期秒数（可选，null表示不修改；小于等于0表示永不过期）
     * @param remark        备注说明（可选，null表示不修改）
     */
    @Override
    public void updateCode(String code, Long expireSeconds, String remark) {
        assertCodeNotEmpty(code);
        OssDownloadCodeInfo info = downloadCodeStorage.get(code);
        assertCodeFound(info, code);
        if (expireSeconds != null) {
            info.setExpireAt(resolveExpireAt(expireSeconds, System.currentTimeMillis()));
        }
        if (remark != null) {
            info.setRemark(remark);
        }
        AssertUtil.isTrue(!downloadCodeStorage.update(info)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_NOT_FOUND, code).throwIt();
    }

    /**
     * 删除下载码
     *
     * @param code 下载唯一码（必填）
     */
    @Override
    public void deleteCode(String code) {
        assertCodeNotEmpty(code);
        AssertUtil.isTrue(!downloadCodeStorage.delete(code)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_NOT_FOUND, code).throwIt();
    }

    /**
     * 解析用于公开下载的唯一码
     * <p>
     * 校验唯一码存在且未过期，错误提示保持通用语义，不泄露存储细节
     * </p>
     *
     * @param code 下载唯一码（必填）
     * @return 校验通过的下载码信息
     */
    @Override
    public OssDownloadCodeInfo resolveForDownload(String code) {
        assertCodeNotEmpty(code);
        OssDownloadCodeInfo info = downloadCodeStorage.get(code);
        AssertUtil.isTrue(info == null || info.isExpired(System.currentTimeMillis()))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_INVALID).throwIt();
        return info;
    }

    /**
     * 分页查询下载码
     * <p>
     * 归一化分页参数并将条件翻译为存储层查询模型，组装总数与当前页数据
     * </p>
     *
     * @param form 查询表单（条件与分页参数）
     * @return 分页结果（总数与当前页数据）
     */
    @Override
    public PageVO<OssDownloadCodeInfo> queryPage(OssDownloadCodeSearchForm form) {
        long pageNum = resolvePageNum(form);
        long pageSize = resolvePageSize(form);
        OssDownloadCodeQuery query = OssDownloadCodeQuery.builder()
            .code(form == null ? null : form.getCode())
            .objectKey(form == null ? null : form.getObjectKey())
            .keyword(form == null ? null : form.getKeyword())
            .status(resolveStatus(form))
            .offset((int)((pageNum - 1) * pageSize))
            .limit((int)pageSize)
            .build();
        List<OssDownloadCodeInfo> data = downloadCodeStorage.list(query);
        long total = downloadCodeStorage.count(query);
        return new PageVO<OssDownloadCodeInfo>().setTotal((int)total).setData(data);
    }

    /**
     * 统计下载码概览
     * <p>
     * 有效数量为总数与已过期数量之差（含永久有效）
     * </p>
     *
     * @return 统计结果（总数/有效/已过期/永久有效）
     */
    @Override
    public OssDownloadCodeStatisticsVO statistics() {
        long total = downloadCodeStorage.count(null);
        long expired = downloadCodeStorage.count(OssDownloadCodeQuery.builder()
            .status(OssDownloadCodeStatus.EXPIRED).build());
        long permanent = downloadCodeStorage.count(OssDownloadCodeQuery.builder()
            .status(OssDownloadCodeStatus.PERMANENT).build());
        OssDownloadCodeStatisticsVO vo = new OssDownloadCodeStatisticsVO();
        vo.setTotal(total);
        vo.setExpired(expired);
        vo.setPermanent(permanent);
        vo.setValid(total - expired);
        return vo;
    }

    /**
     * 解析归一化页码
     *
     * @param form 查询表单（可为空）
     * @return 页码（从1开始）
     */
    private long resolvePageNum(OssDownloadCodeSearchForm form) {
        return form == null || form.getPageNum() == null || form.getPageNum() < 1 ? DEFAULT_PAGE_NUM
            : form.getPageNum();
    }

    /**
     * 解析归一化每页条数
     *
     * @param form 查询表单（可为空）
     * @return 每页条数（不超过上限）
     */
    private long resolvePageSize(OssDownloadCodeSearchForm form) {
        if (form == null || form.getPageSize() == null || form.getPageSize() < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(form.getPageSize(), MAX_PAGE_SIZE);
    }

    /**
     * 解析状态筛选条件
     *
     * @param form 查询表单（可为空）
     * @return 状态枚举，空表示全部；取值非法时报用户输入错误
     */
    private OssDownloadCodeStatus resolveStatus(OssDownloadCodeSearchForm form) {
        if (form == null || StrUtil.isEmpty(form.getStatus())) {
            return null;
        }
        String status = form.getStatus().toUpperCase();
        try {
            return OssDownloadCodeStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
                .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_STATUS_INVALID, form.getStatus()).throwIt();
            return null;
        }
    }

    /**
     * 解析创建时使用的下载码
     * <p>
     * 自定义码为空时自动生成；非空时校验4~64位Base64格式与全局唯一性，非法或冲突报用户输入错误
     * </p>
     *
     * @param code 调用方传入的自定义码（可为空）
     * @return 最终落库使用的下载码
     */
    private String resolveCreateCode(String code) {
        if (StrUtil.isEmpty(code)) {
            return generateUniqueCode();
        }
        AssertUtil.isTrue(!CODE_PATTERN.matcher(code).matches()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_FORMAT_INVALID, code).throwIt();
        AssertUtil.isTrue(downloadCodeStorage.exists(code)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_ALREADY_EXISTS, code).throwIt();
        return code;
    }

    /**
     * 生成全局唯一的16位Base64下载码
     * <p>
     * 12个安全随机字节经URL安全Base64编码（无填充）得16字符，冲突时有限次重试
     * </p>
     *
     * @return 唯一下载码
     */
    private String generateUniqueCode() {
        SecureRandom random = new SecureRandom();
        for (int attempt = 0; attempt < MAX_GENERATE_ATTEMPTS; attempt++) {
            byte[] bytes = new byte[RANDOM_BYTES];
            random.nextBytes(bytes);
            String candidate = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            if (!downloadCodeStorage.exists(candidate)) {
                return candidate;
            }
        }
        AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_GENERATE_FAILED).throwIt();
        return null;
    }

    /**
     * 解析过期时间戳
     *
     * @param expireSeconds 有效期秒数，空或小于等于0表示永不过期
     * @param nowMillis     当前时间戳（毫秒）
     * @return 过期时间戳（毫秒），永不过期时为{@link OssDownloadCodeInfo#NEVER_EXPIRE}
     */
    private long resolveExpireAt(Long expireSeconds, long nowMillis) {
        if (expireSeconds == null || expireSeconds <= 0) {
            return OssDownloadCodeInfo.NEVER_EXPIRE;
        }
        return nowMillis + expireSeconds * 1000L;
    }

    /**
     * 解析实际使用的存储桶名
     *
     * @param bucket 调用方传入的桶名（可为空）
     * @return 缺省时使用默认桶的最终桶名
     */
    private String resolveBucket(String bucket) {
        return StrUtil.isEmpty(bucket) ? ossClient.getDefaultBucket() : bucket;
    }

    /**
     * 断言对象键非空
     *
     * @param objectKey 存储对象键
     */
    private void assertObjectKeyNotEmpty(String objectKey) {
        AssertUtil.isTrue(StrUtil.isEmpty(objectKey)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_OBJECT_KEY_EMPTY).throwIt();
    }

    /**
     * 断言下载码非空
     *
     * @param code 下载唯一码
     */
    private void assertCodeNotEmpty(String code) {
        AssertUtil.isTrue(StrUtil.isEmpty(code)).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_EMPTY).throwIt();
    }

    /**
     * 断言下载码记录存在
     *
     * @param info 查询到的下载码信息
     * @param code 下载唯一码
     */
    private void assertCodeFound(OssDownloadCodeInfo info, String code) {
        AssertUtil.isTrue(info == null).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .i18n(FileProcessI18nConstant.OSS_DOWNLOAD_CODE_NOT_FOUND, code).throwIt();
    }
}
