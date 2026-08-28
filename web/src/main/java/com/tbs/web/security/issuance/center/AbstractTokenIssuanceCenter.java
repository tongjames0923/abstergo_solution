package com.tbs.web.security.issuance.center;

import cn.hutool.core.date.DateUtil;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 抽象的令牌颁发中心
 * <p>
 * 提供了令牌管理的核心实现，包括令牌的创建、保存、查询和状态管理等功能。 该类实现了ITokenIssuanceCenter接口的通用逻辑，并定义了若干抽象方法供具体子类实现。
 * </p>
 * <p>
 * 子类实现规范：
 * <ul>
 *     <li>需实现全部抽象方法，提供令牌模型与关联管理信息的持久化能力；</li>
 *     <li>本类通过 {@link #center} 引用转发内部调用，子类重写具体方法时可被正确路由（模板方法模式）；</li>
 *     <li>过期判定为惰性策略：仅在查询路径（{@link #getTokenByToken}、{@link #getTokensByUserNameAndTokenType}）
 *     触发状态修正并回写，不依赖定时任务扫描；</li>
 *     <li>若子类对令牌查询增加缓存，须在 {@link #saveToken}、{@link #saveManageInfo} 等写路径同步失效，
 *     避免出现状态已变更但缓存仍为旧值的脏读。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @see ITokenIssuanceCenter 令牌颁发中心对外契约
 */
public abstract class AbstractTokenIssuanceCenter implements ITokenIssuanceCenter {

    /**
     * 根据令牌字符串获取令牌模型
     * <p>
     * 抽象方法，由子类实现具体的令牌查询逻辑。本方法是令牌检索的唯一入口，
     * 若子类引入缓存应在此实现，并与 {@link #saveToken} 的写路径保持失效联动。
     * </p>
     *
     * @param token 令牌字符串，不能为空
     * @return 对应的令牌模型，如果不存在则返回 {@code null}
     */
    protected abstract ITokenModel getModel( String token);

    /**
     * 推荐令牌到期时间
     * <p>
     * 根据令牌类型读取 {@link WebAuthProperty} 中对应的有效期配置，在基准时间上推算到期时刻：
     * <ul>
     *     <li>{@link TokenTypeEnum#REFRESH_TOKEN}：按刷新令牌有效期，单位小时；</li>
     *     <li>{@link TokenTypeEnum#WEB_ACCESS_TOKEN}：按访问令牌有效期，单位秒；</li>
     *     <li>{@link TokenTypeEnum#APPLICATION_ACCESS_TOKEN}：按应用令牌有效期，单位小时。</li>
     * </ul>
     * 子类可重写以接入自定义有效期策略（如按用户等级、租户差异化）。
     * </p>
     *
     * @param now       基准时间，通常为当前时间
     * @param tokenType 令牌类型，取值来自 {@link TokenTypeEnum#getCode()}
     * @return 推荐的过期时间；令牌类型无匹配配置时返回 {@code null}
     */
    protected Date recommendExpireTime(Date now, int tokenType) {
        WebAuthProperty authProperty = SpringUtil.getBean(WebAuthProperty.class);
        Date exp = null;
        if (Objects.equals(tokenType, TokenTypeEnum.REFRESH_TOKEN.getCode())) {
            exp = new Date(now.getTime() + TimeUnit.HOURS.toMillis(authProperty.getRefreshTokenExpireTime()));
        } else if (Objects.equals(tokenType, TokenTypeEnum.WEB_ACCESS_TOKEN.getCode())) {
            exp = new Date(now.getTime() + TimeUnit.SECONDS.toMillis(authProperty.getTokenExpireTime()));
        } else if (Objects.equals(tokenType, TokenTypeEnum.APPLICATION_ACCESS_TOKEN.getCode())) {
            exp = new Date(now.getTime() + TimeUnit.HOURS.toMillis(authProperty.getApplicationTokenExpireTime()));
        }
        return exp;
    }

    /**
     * 保存令牌模型
     * <p>
     * 执行令牌保存前的参数校验，并按顺序持久化管理信息与令牌本体：
     * 先 {@link #saveManageInfo} 维护「用户名-令牌类型-令牌」索引，再 {@link #saveToken} 写入令牌模型。
     * 校验不通过时抛出国际化异常，不会触发任何持久化动作。
     * </p>
     *
     * @param model 令牌模型，其令牌串、用户名、令牌类型均不能为空
     */
    protected void saveModel( ITokenModel model) {
        AssertUtil.hasText(model.getToken(), I18nUtil.get("token.required"));
        AssertUtil.hasText(model.getUserName(), I18nUtil.get("user.name.required"));
        AssertUtil.isTrue(model.getTokenType() == null, I18nUtil.get("token.type.required"));
        center.saveManageInfo(model.getUserName(), model.getTokenType().getCode(), model.getToken());
        center.saveToken(model);
    }

    /**
     * 保存令牌
     * <p>
     * 抽象方法，由子类实现具体的令牌保存逻辑。由 {@link #saveModel} 在管理信息写成功后调用，
     * 实现需同时兼容新增与状态更新场景。
     * </p>
     *
     * @param model 令牌模型，不能为空
     */
    protected abstract void saveToken(ITokenModel model);

    /**
     * 保存令牌管理信息
     * <p>
     * 抽象方法，由子类实现具体的令牌管理信息保存逻辑。
     * 用于维护「用户名-令牌类型-令牌」索引，支撑按用户与类型批量检索令牌。
     * </p>
     *
     * @param userName  用户名，不能为空
     * @param tokenType 令牌类型，取值来自 {@link TokenTypeEnum#getCode()}
     * @param token     令牌字符串，不能为空
     */
    protected abstract void saveManageInfo(String userName, int tokenType, String token);

    /**
     * 根据用户名和令牌类型获取令牌集合
     * <p>
     * 抽象方法，由子类实现具体的查询逻辑。通常由 {@link #saveManageInfo} 维护的索引支撑检索。
     * 返回的集合中若含 {@code null} 元素，会被 {@link #getTokensByUserNameAndTokenType} 跳过。
     * </p>
     *
     * @param userName  用户名，不能为空
     * @param tokenType 令牌类型，取值来自 {@link TokenTypeEnum#getCode()}
     * @return 用户名和令牌类型对应的令牌模型集合，无匹配时返回空集合
     */
    protected abstract Collection<ITokenModel> getUserNameAndTokenType( String userName, int tokenType);

    /**
     * 根据会话ID获取令牌集合
     * <p>
     * 抽象方法，由子类实现具体的查询逻辑。用于会话级令牌管理，如会话批量登出。
     * </p>
     *
     * @param sessionId 会话ID，不能为空
     * @return 该会话下的令牌模型集合，无匹配时返回空集合
     */
    protected abstract Collection<ITokenModel> getTokensWithSessionId(String sessionId);

    /**
     * 列出指定用户在指定时间段内的会话令牌
     * <p>
     * 抽象方法，由子类实现具体的查询逻辑。入参时间范围由 {@link #listSessions} 保证非 {@code null}，
     * 子类实现无需再做兜底。
     * </p>
     *
     * @param userName 用户名，不能为空
     * @param beg      开始时间，非 {@code null}
     * @param end      结束时间，非 {@code null}
     * @return 该用户在时间范围内的会话令牌集合，无匹配时返回空集合
     */
    protected abstract Collection<ITokenModel> listSessionsByUserName(String userName, Date beg, Date end);


    /**
     * 创建令牌模型
     * <p>
     * 抽象方法，由子类实现具体的令牌创建逻辑。实现需生成唯一令牌串，并结合
     * {@link #recommendExpireTime} 给出的建议到期时间组装模型。本方法仅创建对象，不执行持久化。
     * </p>
     *
     * @param form 令牌请求表单，含用户信息、令牌类型、创建方式与会话ID，不能为空
     * @return 新创建的令牌模型，尚未持久化
     */
    protected abstract ITokenModel createModel( TokenRequestForm form);

    /**
     * 指向当前实例的引用，用于在构造函数中初始化。
     * <p>
     * 通过它转发内部调用，使子类对抽象方法的实现可被本类模板逻辑正确路由。
     * </p>
     */
    private AbstractTokenIssuanceCenter center;

    /**
     * 获取当前实例引用
     *
     * @return 当前令牌颁发中心实例
     */
    protected AbstractTokenIssuanceCenter getInstance() {
        return center;
    }

    /**
     * 构造函数，初始化center引用
     */
    public AbstractTokenIssuanceCenter() {
        this.center = this;
    }

    /**
     * 发放令牌
     * <p>
     * 校验请求表单必填项后，创建令牌模型并保存：先由 {@link #createModel} 生成模型，
     * 再由 {@link #saveModel} 持久化管理信息与令牌本体。校验失败时抛出国际化异常，不会生成令牌。
     * </p>
     *
     * @param form 令牌请求表单，不能为空；其用户信息、用户名、令牌类型、创建类型、会话ID均不能为空
     * @return 创建并保存后的令牌模型
     */
    @Override
    public ITokenModel issueToken(TokenRequestForm form) {
        AssertUtil.notNull(form, I18nUtil.get("token.request.required"));
        AssertUtil.notNull(form.getUserDetails(), I18nUtil.get("user.info.required"));
        AssertUtil.hasText(form.getUserDetails().getUsername(), I18nUtil.get("user.name.required"));
        AssertUtil.isTrue(form.getTokenType() == null, I18nUtil.get("token.type.required"));
        AssertUtil.isTrue(form.getTokenCreateType() == null, I18nUtil.get("token.create.type.required"));
        AssertUtil.hasText(form.getSessionId(), I18nUtil.get("token.session.id.required"));
        ITokenModel model = createModel(form);
        center.saveModel(model);
        return model;
    }

    /**
     * 根据令牌字符串获取令牌模型
     * <p>
     * 查询指定令牌，并执行惰性过期处理：若令牌已过期且状态仍为生效中，
     * 会将其状态置为已过期并回写存储，返回模型的状态同步为已过期。
     * </p>
     *
     * @param token 令牌字符串，不能为空
     * @return 对应的令牌模型（状态已按过期情况修正）；令牌不存在时返回 {@code null}
     */
    @Override
    public ITokenModel getTokenByToken( String token) {
        AssertUtil.hasText(token, I18nUtil.get("token.required"));
        ITokenModel model = center.getModel(token);
        if (model == null) {
            return model;
        }
        return setExpireStatus(model);
    }

    /**
     * 设置令牌过期状态
     * <p>
     * 检查令牌是否已过期，如果已过期且状态为生效中，则更新其状态为已过期并回写存储。
     * 状态非生效中（如已撤销、已删除）时仅修正内存模型，不重复回写。
     * </p>
     *
     * @param model 待检查的令牌模型
     * @return 更新状态后的令牌模型，与原入参为同一实例
     */
    private ITokenModel setExpireStatus(ITokenModel model) {
        if (model.getExpireTime() != null && model.getExpireTime().getTime() < System.currentTimeMillis()) {
            if (TokenStatusEnum.ACTIVE.getCode().equals(model.getStatus().getCode())) {
                center.setTokenStatus(model.getToken(), TokenStatusEnum.EXPIRED);
            }
            model.setStatus(TokenStatusEnum.EXPIRED);
        }
        return model;
    }

    /**
     * 设置令牌状态
     * <p>
     * 查询令牌并更新其状态后保存（走 {@link #saveModel}，同步刷新管理信息）。
     * 令牌不存在时抛出国际化异常。
     * </p>
     *
     * @param token  令牌字符串，不能为空
     * @param status 新的令牌状态，不能为空，取值见 {@link TokenStatusEnum}
     */
    @Override
    public void setTokenStatus( String token,  TokenStatusEnum status) {
        ITokenModel oldModel = center.getModel(token);
        AssertUtil.notNull(oldModel, I18nUtil.get("token.not.found"));
        oldModel.setStatus(status);
        center.saveModel(oldModel);
    }

    /**
     * 根据用户名和令牌类型获取令牌集合
     * <p>
     * 查询指定用户和令牌类型的所有令牌，逐个执行过期处理，并按令牌串去重后返回。
     * </p>
     *
     * @param userName  用户名，不能为空
     * @param tokenType 令牌类型，取值来自 {@link TokenTypeEnum#getCode()}
     * @return 用户名和令牌类型对应的令牌模型集合；已按令牌串去重，无匹配时返回空集合
     */
    @Override
    public Collection<ITokenModel> getTokensByUserNameAndTokenType(String userName, int tokenType) {
        Map<String, ITokenModel> tokens = new HashMap<>(8);
        for (ITokenModel model : center.getUserNameAndTokenType(userName, tokenType)) {
            if (model == null) {
                continue;
            }
            model = setExpireStatus(model);
            tokens.putIfAbsent(model.getToken(), model);
        }
        return tokens.values();
    }

    /**
     * 根据会话ID获取令牌集合
     * <p>
     * 查询归属于同一会话的全部令牌，用于会话级令牌管理与批量登出。
     * 注意：本方法不执行过期状态修正，返回的是存储中的原始状态。
     * </p>
     *
     * @param sessionId 会话ID，不能为空
     * @return 该会话下的令牌模型集合，无匹配时返回空集合
     */
    @Override
    public Collection<ITokenModel> getTokensBySessionId(String sessionId) {
        return center.getTokensWithSessionId(sessionId);
    }

    /**
     * 列出指定用户在指定时间段内的会话令牌
     * <p>
     * 时间范围为空时自动兜底：起始时间默认取当前时间往前 168 天（28×6），结束时间默认为当前时间。
     * 注意：本方法不执行过期状态修正，返回的是存储中的原始状态。
     * </p>
     *
     * @param userName 用户名，不能为空
     * @param beg      开始时间，为 {@code null} 时按默认窗口兜底
     * @param end      结束时间，为 {@code null} 时取当前时间
     * @return 该用户在时间范围内的会话令牌集合，无匹配时返回空集合
     */
    @Override
    public Collection<ITokenModel> listSessions(String userName, Date beg, Date end) {
        AssertUtil.hasText(userName, I18nUtil.get("user.name.required"));
        if (beg == null) {
            beg = DateUtil.offsetDay(new Date(), -(28*6));
        }
        if (end == null) {
            end = new Date();
        }
        return listSessionsByUserName(userName, beg, end);
    }
}
