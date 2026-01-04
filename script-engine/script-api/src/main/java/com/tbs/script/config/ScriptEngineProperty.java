package com.tbs.script.config;

import com.tbs.script.api.engine.IScriptEngine;
import com.tbs.script.api.engine.impl.SpelScriptEngine;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class ScriptEngineProperty {

    /**
     * 脚本引擎类
     */
    private Class<? extends IScriptEngine> engineClass = SpelScriptEngine.class;
}
