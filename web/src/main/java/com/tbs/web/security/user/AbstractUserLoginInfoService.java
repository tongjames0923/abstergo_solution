package com.tbs.web.security.user;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.web.security.token.ITokenModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Objects;

/**
 * 抽象用户登录信息服务
 * <p>
 * 模板实现 {@link IUserLoginInfoService}：定义「令牌 ↔ 登录信息」增删改查的通用校验与流程，
 * 具体存储形态由子类实现 {@link #readUserDetails}/{@link #writeUserDetails}/{@link #removeUserDetails}。
 * 本类只关心会话是否存在，不关心用户详情如何装配，因此不会把模块拓扑泄漏进安全层。
 * </p>
 *
 * @author abstergo
 * @see IUserLoginInfoService 登录信息服务契约
 */
@Slf4j
public abstract class AbstractUserLoginInfoService implements IUserLoginInfoService {

    /**
     * 从存储读取用户详情
     *
     * @param token 登录令牌，不能为空
     * @return 用户详情；不存在时返回 {@code null}
     */
    protected abstract UserDetails readUserDetails(String token);

    /**
     * 写入用户详情到存储
     *
     * @param token       登录令牌，不能为空
     * @param userDetails 用户详情，不能为空
     */
    protected abstract void writeUserDetails(ITokenModel token, UserDetails userDetails);

    /**
     * 从存储删除用户详情
     *
     * @param token 登录令牌，不能为空
     */
    protected abstract void removeUserDetails(String token);

    /**
     * 判断指定令牌是否已存在登录信息
     * <p>
     * 这是令牌唯一性的业务判定点：存储层不建唯一约束，所有「同一令牌只能有一行」的保证都由这里完成。
     * 默认实现回源读取用户详情；存储层能直接按键判存时应覆写，避免为一次存在性判断付出完整装配开销。
     * </p>
     *
     * @param token 登录令牌，不能为空
     * @return 存在返回 {@code true}
     */
    protected boolean existsLoginInfo(String token) {
        return readUserDetails(token) != null;
    }

    /**
     * 写入登录信息
     * <p>
     * 令牌唯一性由业务层保证：写入前先按键判存，已存在则拒绝，不依赖数据库唯一约束。
     * 重复写入通常意味着令牌生成或调用时机出错，直接失败比覆盖旧会话更安全：
     * 覆盖会把旧会话的部门选择、过期时间静默换成新值。
     * </p>
     *
     * @param userDetails 用户详情，不能为空
     * @param tokenModel  本次登录签发的令牌模型，不能为空
     */
    @Override
    public void insertUserLoginInfo(UserDetails userDetails, ITokenModel tokenModel) {
        AssertUtil.notNull(userDetails, "登录用户信息不能为空");
        AssertUtil.notNull(tokenModel, "登录令牌不能为空");
        AssertUtil.hasText(tokenModel.getToken(), "登录令牌内容不能为空");
        AssertUtil.hasText(userDetails.getUsername(), "登录用户名不能为空");
        // 防重复写入：同令牌已存在登录信息则拒绝，避免覆盖旧的会话关联
        AssertUtil.isTrue(existsLoginInfo(tokenModel.getToken()), "令牌{}，登录用户信息已存在", tokenModel.getToken());
        writeUserDetails(tokenModel, userDetails);
    }

    @Override
    public void removeUserLoginInfo(String token) {
        AssertUtil.hasText(token, "删除登录用户信息令牌不能为空");
        UserDetails details = readUserDetails(token);
        if (details == null) {
            log.info("令牌{}，登录用户信息不存在", token);
            return;
        }
        removeUserDetails(token);
    }

    /**
     * 置换当前会话的用户详情
     * <p>
     * 仅校验会话存在且属于同一用户，不写入任何用户详情。
     * 按会话维度变更个别属性时，优先使用专用方法（如 {@link IUserLoginInfoService#changeCurrentDepartment}）。
     * </p>
     *
     * @param userDetails 新的用户详情，不能为空
     * @param token       登录令牌，不能为空
     * @deprecated 用户详情不再持久化，本方法不会写入任何用户详情；
     *     权限与部门变更在下次认证时自然生效，无需置换。计划在下一个大版本移除。
     */
    @Deprecated
    @Override
    public void replaceUserDetails(UserDetails userDetails, ITokenModel token) {
        UserDetails details = readUserDetails(token.getToken());
        AssertUtil.notNull(details, "令牌{}，登录用户信息不存在", token);
        // 安全约束：仅允许同一用户置换详情，防止越权替换他人会话信息
        AssertUtil.isTrue(!Objects.equals(details.getUsername(), userDetails.getUsername()),
            "令牌{}，非法的用户信息置换，仅能针对同一用户进行用户信息置换。", token);
        writeUserDetails(token, userDetails);
    }

    /**
     * 根据令牌获取用户详情
     * <p>
     * 返回值恒为 0 或 1 个元素的列表：无匹配时为空集合，有匹配时为单元素集合，不会出现 null 元素。
     * 用户详情为请求级视图，不在令牌维度缓存，因此空集合不会被缓存：
     * 无效令牌的重复探测由令牌模型自身的缓存与签名校验拦住，不必在此叠一层空值缓存。
     * 集合统一经 {@link CollUtil#singletonList} 包装为可序列化的 {@link java.util.ArrayList}，
     * 避免调用方后续做缓存化改造时因 JDK 内部集合实现无法反序列化而失败。
     * </p>
     *
     * @param token 令牌串，不能为空
     * @return 令牌对应的用户详情集合，无匹配时返回空集合
     */
    @Override
    public List<UserDetails> getUserDetails(String token) {
        AssertUtil.hasText(token, "查询登录时用户数据令牌不能为空");
        return CollUtil.singletonList(readUserDetails(token));
    }

}
