package com.tbs.web.security.issuance.center;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.form.TokenRequestForm;
import com.tbs.web.security.token.ITokenModel;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 抽象的令牌颁发中心
 * <p>
 * 提供了令牌管理的核心实现，包括令牌的创建、保存、查询和状态管理等功能。 该类实现了ITokenIssuanceCenter接口的通用逻辑，并定义了若干抽象方法供具体子类实现。
 * </p>
 *
 * @author abstergo
 */
public abstract class AbstractTokenIssuanceCenter implements ITokenIssuanceCenter {

    /**
     * 根据令牌字符串获取令牌模型
     * <p>
     * 抽象方法，由子类实现具体的令牌查询逻辑。
     * </p>
     *
     * @param token 令牌字符串，不能为空
     * @return 对应的令牌模型，如果不存在则返回null
     */
    protected abstract ITokenModel getModel(@NotNull String token);

    /**
     * 推荐令牌到期时间
     * <p>
     * 根据令牌类型计算并返回推荐的过期时间。 对于刷新令牌，使用刷新令牌过期时间配置；对于其他令牌，使用普通令牌过期时间配置。
     * </p>
     *
     * @param now       当前时间
     * @param tokenType 令牌类型
     * @return 推荐的过期时间
     */
    protected Date recommendExpireTime(Date now, int tokenType) {
        WebAuthProperty authProperty = SpringUtil.getBean(WebAuthProperty.class);
        Date exp = null;
        if (tokenType == TokenTypeEnum.REFRESH_TOKEN.getCode()) {
            exp = new Date(now.getTime() + TimeUnit.HOURS.toMillis(authProperty.getRefreshTokenExpireTime()));
        } else {
            exp = new Date(now.getTime() + TimeUnit.SECONDS.toMillis(authProperty.getTokenExpireTime()));
        }
        return exp;
    }

    /**
     * 保存令牌模型
     * <p>
     * 执行令牌保存前的验证，并调用抽象方法保存令牌及管理信息。
     * </p>
     *
     * @param model 令牌模型，不能为空
     */
    protected void saveModel(@NotNull ITokenModel model) {
        AssertUtil.hasText(model.getToken(), "令牌不能为空");
        AssertUtil.hasText(model.getUserName(), "用户名不能为空");
        AssertUtil.isTrue(model.getTokenType() == null, "令牌类型不能为空");
        center.saveManageInfo(model.getUserName(), model.getTokenType().getCode(), model.getToken());
        center.saveToken(model);
    }

    /**
     * 保存令牌
     * <p>
     * 抽象方法，由子类实现具体的令牌保存逻辑。
     * </p>
     *
     * @param model 令牌模型
     */
    protected abstract void saveToken(ITokenModel model);

    /**
     * 保存令牌管理信息
     * <p>
     * 抽象方法，由子类实现具体的令牌管理信息保存逻辑。
     * </p>
     *
     * @param userName  用户名
     * @param tokenType 令牌类型
     * @param token     令牌字符串
     */
    protected abstract void saveManageInfo(String userName, int tokenType, String token);

    /**
     * 根据用户名和令牌类型获取令牌集合
     * <p>
     * 抽象方法，由子类实现具体的查询逻辑。
     * </p>
     *
     * @param userName  用户名，不能为空
     * @param tokenType 令牌类型
     * @return 用户名和令牌类型对应的令牌模型集合
     */
    protected abstract Collection<ITokenModel> getUserNameAndTokenType(@NotNull String userName, int tokenType);

    /**
     * 根据会话ID获取令牌集合
     *
     * @param sessionId
     * @return
     */
    protected abstract Collection<ITokenModel> getTokensWithSessionId(String sessionId);

    /**
     * 创建令牌模型
     * <p>
     * 抽象方法，由子类实现具体的令牌创建逻辑。
     * </p>
     *
     * @param userDetails     用户详细信息，不能为空
     * @param tokenType       令牌类型
     * @param tokenCreateType 令牌创建类型
     * @return 新创建的令牌模型
     */
    protected abstract ITokenModel createModel(@NotNull TokenRequestForm form);

    /**
     * 指向当前实例的引用，用于在构造函数中初始化
     */
    private AbstractTokenIssuanceCenter center;

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
     * 根据用户详细信息、令牌类型和令牌创建类型创建新的令牌模型，并保存该模型。
     * </p>
     *
     * @param form@return 创建并保存后的令牌模型
     */
    @Override
    public ITokenModel issueToken(TokenRequestForm form) {
        AssertUtil.notNull(form, "令牌请求参数不能为空");
        AssertUtil.notNull(form.getUserDetails(), "用户信息不能为空");
        AssertUtil.hasText(form.getUserDetails().getUsername(), "用户名不能为空");
        AssertUtil.isTrue(form.getTokenType() == null, "令牌类型不能为空");
        AssertUtil.isTrue(form.getTokenCreateType() == null, "令牌创建类型不能为空");
        AssertUtil.hasText(form.getSessionId(), "会话ID不能为空");
        ITokenModel model = createModel(form);
        center.saveModel(model);
        return model;
    }

    /**
     * 根据令牌字符串获取令牌模型
     * <p>
     * 查询指定令牌，并检查其是否已过期，如果已过期则更新其状态。
     * </p>
     *
     * @param token 令牌字符串，不能为空
     * @return 对应的令牌模型，如果令牌不存在则返回null
     */
    @Override
    public ITokenModel getTokenByToken(@NotNull String token) {
        ITokenModel model = center.getModel(token);
        if (model == null) {
            return model;
        }
        return setExpireStatus(model);
    }

    /**
     * 设置令牌过期状态
     * <p>
     * 检查令牌是否已过期，如果已过期且状态为活跃，则更新其状态为已过期。
     * </p>
     *
     * @param model 令牌模型
     * @return 更新状态后的令牌模型
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
     * 更新指定令牌的状态，并保存更新后的令牌模型。
     * </p>
     *
     * @param token  令牌字符串，不能为空
     * @param status 新的令牌状态，不能为空
     */
    @Override
    public void setTokenStatus(@NotNull String token, @NotNull TokenStatusEnum status) {
        ITokenModel oldModel = center.getModel(token);
        AssertUtil.notNull(oldModel, "令牌不存在");
        oldModel.setStatus(status);
        center.saveModel(oldModel);
    }

    /**
     * 根据用户名和令牌类型获取令牌集合
     * <p>
     * 查询指定用户和令牌类型的所有令牌，并检查每个令牌是否已过期。
     * </p>
     *
     * @param userName  用户名
     * @param tokenType 令牌类型
     * @return 用户名和令牌类型对应的令牌模型集合
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

    @Override
    public Collection<ITokenModel> getTokensBySessionId(String sessionId) {
        return center.getTokensWithSessionId(sessionId);
    }
}
