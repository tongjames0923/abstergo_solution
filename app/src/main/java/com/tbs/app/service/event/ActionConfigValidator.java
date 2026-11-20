package com.tbs.app.service.event;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.script.api.expressible.impl.TriggerActionQueryService;
import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * 动作绑定配置校验器。
 *
 * <p>动作实例来自脚本模块注册中心，而动作绑定的持久化在 system 模块，
 * 因此「动作是否已注册」的校验放在接入层完成，供控制器保存前调用；
 * 参数 JSON 的结构校验由服务层负责，二者职责不重叠。</p>
 *
 * @author abstergo
 */
@Service
@Slf4j
public class ActionConfigValidator {

    /**
     * 动作元信息与实例查询服务
     */
    @Resource
    private TriggerActionQueryService triggerActionQueryService;

    /**
     * 校验动作已注册。
     *
     * @param actionId 动作ID
     */
    public void assertActionExists(String actionId) {
        AssertUtil.isTrue(StrUtil.isBlank(actionId) || triggerActionQueryService.getActionInstance(actionId) == null)
            .i18n("event.action.not.registered", actionId).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * 校验一组动作绑定引用的动作均已注册。
     *
     * @param actions 动作绑定集合
     */
    public void assertBindings(Collection<SystemEventActionEntity> actions) {
        if (CollUtil.isEmpty(actions)) {
            return;
        }
        for (SystemEventActionEntity action : actions) {
            if (action == null) {
                continue;
            }
            assertActionExists(action.getActionId());
        }
    }
}
