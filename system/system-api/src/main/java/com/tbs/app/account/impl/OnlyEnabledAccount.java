package com.tbs.app.account.impl;

import com.tbs.app.account.IAccountStatusValidator;
import com.tbs.app.account.exception.UnNormalAccountStatusException;
import com.tbs.app.account.vo.AccountVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.enums.SystemUserStatusEnum;

import java.util.Objects;

/**
 * 仅允许启用状态的账户
 *
 * @author abstergo
 */
public class OnlyEnabledAccount implements IAccountStatusValidator {
    @Override
    public void validate(AccountVO vo) throws UnNormalAccountStatusException {
        AssertUtil.isTrue(!Objects.equals(SystemUserStatusEnum.ACTIVE.getCode(), vo.getFlag()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).message("用户{}已被{}!")
            .params(vo.getUserName(), EnumUtil.valueOf(SystemUserStatusEnum.class, vo.getFlag()).getValue()).as((f) -> {
                var r = new UnNormalAccountStatusException(f, f.getMessage());
                r.errorLevel(f.getLevel());
                return r;
            }).throwIt();
        ;
    }
}
