package com.tbs.dic.config;

import com.tbs.common.i18n.AbstractMessageSourceProvider;
import org.springframework.stereotype.Component;

/**
 * Dic 模块消息源提供者
 * <p>
 * 自动注册 dic 模块的消息资源文件：messages-dic.properties
 *
 * @author abstergo
 */
@Component
public class DicMessageSourceProvider extends AbstractMessageSourceProvider {

    @Override
    public String getBasename() {
        return "messages-dic";
    }

    @Override
    public int getOrder() {
        // 数据字典模块优先级
        return -300;
    }
}
