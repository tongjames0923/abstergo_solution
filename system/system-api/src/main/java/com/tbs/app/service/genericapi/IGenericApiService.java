package com.tbs.app.service.genericapi;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;
import com.tbs.web.genric.IApiInstancePreAuth;

import java.util.List;

/**
 * 泛化API配置服务接口
 * <p>
 * 提供泛化API配置的增删改查及启停用功能
 * </p>
 *
 * @author abstergo
 */
public interface IGenericApiService extends IServiceTemplate<SystemGenericApiEntity, Long> {

    /**
     * 获取所有可用的预认证列表
     *
     * @return
     */
    List<IApiInstancePreAuth> getAllAvaliablePreAuthList();

    /**
     * 根据API ID查询配置
     *
     * @param apiId API唯一标识
     * @return 泛化API配置实体
     */
    SystemGenericApiEntity getByApiId(String apiId);

    /**
     * 取可被泛化调用的 API 配置
     * <p>
     * 「配置存在」与「已启用」两条规则集中在服务层断言，新版泛化调用入口只做取数与映射。
     * </p>
     *
     * @param apiId API唯一标识
     * @return 已启用的泛化API配置，不会为 {@code null}
     */
    SystemGenericApiEntity getInvokableApi(String apiId);

    /**
     * 取可从匿名入口泛化调用的 API 配置
     * <p>
     * 在 {@link #getInvokableApi(String)} 基础上追加免认证约束：登记为需要认证的 API 禁止走匿名入口。
     * </p>
     *
     * @param apiId API唯一标识
     * @return 已启用且无需认证的泛化API配置，不会为 {@code null}
     */
    SystemGenericApiEntity getPublicInvokableApi(String apiId);

    /**
     * 保存泛化API配置，并整组替换其入参定义
     * <p>
     * 配置面一个表单同时提交「API 声明」与「入参定义」，两写必须在同一事务内：主表成功而参数失败会留下
     * 无可调用元数据的半成品配置。参数采用整组替换（删旧插新）：参数行的语义全部由 {@code paramOrder}
     * 承载，逐行比对与整组替换结果等价，而后者不会漏掉被删行。
     * </p>
     *
     * @param apiConfig 主表配置（无 id 则新增，有 id 则更新）
     * @param params    入参定义列表，可以为空（表示该方法无入参，同时清除旧登记）
     * @return 保存后的主表配置
     */
    SystemGenericApiEntity saveWithParams(SystemGenericApiEntity apiConfig, List<SystemGenericApiParamEntity> params);

    /**
     * 启用API配置
     *
     * @param apiId API唯一标识
     */
    void enableApi(String apiId);

    /**
     * 禁用API配置
     *
     * @param apiId API唯一标识
     */
    void disableApi(String apiId);
}
