package com.tbs.app.service;

import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.interfaces.service.AbstractAuditService;
import com.tbs.web.uitl.AuthUtil;

public class DefaultAuditService<E, K> extends AbstractAuditService<E, K, Long> {
    @Override
    public Long getCurrentUserId() {
        var systemUserService = SpringUtil.getBeanOptional(ISystemUserService.class);
        AssertUtil.isTrue(systemUserService.isEmpty()).errorLevel(ErrorLevelEnum.SYSTEM_ERROR)
            .message("系统用户服务未初始化").throwIt();
        var us = systemUserService.get();
        return CollUtil.getFirst(us.queryUserIdsByUserName(AuthUtil.getInstance().getCurrentUser().getUsername()));
    }
}
