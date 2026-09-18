package com.tbs.push.websocket.annotation;

import com.tbs.push.config.PushPackAutoRegister;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * WebSocket启用注解的装配范围选择器
 * <p>
 * 使@EnableTbsPushWebSocket额外携带@EnableTbsPush的装配范围（PushPackAutoRegister），
 * 参照厂商impl模块覆盖抽象层装配的模式，业务侧无需重复标注两个注解。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class EnableTbsPushRegistrar implements ImportSelector {

    /**
     * 选择需要导入的抽象层注册器
     *
     * @param importingClassMetadata 导入类元数据
     * @return 需要导入的类名数组
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[] {PushPackAutoRegister.class.getName()};
    }

}
