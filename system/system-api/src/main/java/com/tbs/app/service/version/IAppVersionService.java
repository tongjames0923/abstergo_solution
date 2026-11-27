package com.tbs.app.service.version;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.version.AppVersionEntity;

import java.util.List;

/**
 * 应用版本服务
 * <p>
 * 提供应用版本的基础增删改查与生命周期状态流转能力，
 * 状态流转路径：DRAFT(0) -> PENDING(2) -> RELEASE(1) -> OFFLINE(-1) -> ARCHIVED(-3)，
 * 其中 RELEASE 可直接流转至 ROLLED_BACK(-2)。
 * 非法的状态流转由实现层通过断言拦截。
 *
 * @author abstergo
 */
public interface IAppVersionService extends IServiceTemplate<AppVersionEntity, Long> {

    /**
     * 提交审核
     * <p>
     * 将草稿状态的版本提交审核，状态由 DRAFT 流转为 PENDING
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    AppVersionEntity submit(Long id);

    /**
     * 发布版本
     * <p>
     * 将待审核状态的版本正式发布，状态由 PENDING 流转为 RELEASE，并记录发布时间
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    AppVersionEntity release(Long id);

    /**
     * 下线版本
     * <p>
     * 将已发布状态的版本下线，状态由 RELEASE 流转为 OFFLINE，并记录下线时间
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    AppVersionEntity offline(Long id);

    /**
     * 回滚版本
     * <p>
     * 将已发布状态的版本回滚，状态由 RELEASE 流转为 ROLLED_BACK
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    AppVersionEntity rollback(Long id);

    /**
     * 归档版本
     * <p>
     * 将已下线状态的版本归档，状态由 OFFLINE 流转为 ARCHIVED
     *
     * @param id 版本ID
     * @return 流转后的版本实体
     */
    AppVersionEntity archive(Long id);

    /**
     * 查询指定应用与平台下的最新已发布正式版本，历史 NULL 类型视为正式版
     *
     * @param appKey   应用标识
     * @param platform 平台标识，如 ANDROID、IOS、WEB
     * @return 最新已发布正式版本，不存在时返回 null
     */
    AppVersionEntity queryLatestReleased(String appKey, String platform);

    /**
     * 查询已登记版本的应用标识清单
     * <p>
     * 对未归档版本的 appKey 去重汇总，供兼容策略选择源/目标应用时使用，
     * 兼容限制只能作用于已登记应用的场景
     *
     * @return 去重并按字典序排序的应用标识列表，无数据时返回空集合
     */
    List<String> listAppKeys();

    /**
     * 基于已有版本复制新建
     * <p>
     * 新版本为草稿，versionCode/versionName 由调用方指定；可选将源版本的安装包复制到新版本，
     * 对象存储文件会复制到新 key，两个版本互不共享文件。
     * </p>
     *
     * @param sourceId     来源版本ID
     * @param override     新版本的字段覆盖值，id 会被忽略
     * @param copyPackages 是否复制安装包
     * @return 新建的版本实体
     */
    AppVersionEntity cloneVersion(Long sourceId, AppVersionEntity override, boolean copyPackages);
}
