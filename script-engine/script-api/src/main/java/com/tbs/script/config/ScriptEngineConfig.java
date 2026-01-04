package com.tbs.script.config;

import com.tbs.common.util.base.BeanUtil;
import com.tbs.script.api.engine.IScriptEngine;
import com.tbs.script.api.engine.impl.SpelScriptEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 脚本引擎配置
 *
 * @author tongj
 */
@Configuration
public class ScriptEngineConfig {
    /**
     * 脚本引擎属性
     */
    private ScriptEngineProperty property;

    /**
     * 默认脚本引擎
     *
     * @return 默认脚本引擎
     */
    @Bean
    public IScriptEngine defaultScriptEngine() {
        return BeanUtil.buildByEmptyArgs(SpelScriptEngine.class);
    }
}
