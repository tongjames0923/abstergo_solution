package com.tbs.app.account;

import com.tbs.app.account.exception.UnNormalAccountStatusException;
import com.tbs.app.account.vo.AccountVO;

/**
 * 账户状态验证接口
 * <p>
 * 该接口定义了账户状态验证的抽象方法，用于验证账户状态并返回相应的结果。
 * </p>
 *
 * @author abstergo
 */
public interface IAccountStatusValidator {

    /**
     * 验证账户状态
     *
     * @param vo 账户信息
     * @return 账户状态验证结果
     */
    public void validate(AccountVO vo) throws UnNormalAccountStatusException;
}
