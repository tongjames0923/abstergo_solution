package com.tbs.script.api.engine;

import com.tbs.common.IStartup;
import com.tbs.common.error.AssertUtil;
import com.tbs.script.api.compiler.IScriptCompiler;
import com.tbs.script.api.compiler.IScriptCompilerMeta;
import com.tbs.script.api.context.IScriptContextFactory;
import com.tbs.script.api.context.impl.EmptySimpleScriptContextFactory;
import com.tbs.script.model.ScriptCompilerSpec;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 抽象脚本引擎
 *
 * @author tongj
 */

@Slf4j
public abstract class AbstractScriptEngine implements IScriptEngine, IStartup {

    private Map<String, ScriptCompilerSpec> compilerSpecs = new HashMap<>();

    private String makeSpecKey(String compilerName, String version) {
        return compilerName + ":" + version;
    }

    private String versionString(Integer major, Integer minor) {
        if (major == null) {
            major = 0;
        }
        if (minor == null) {
            minor = 0;
        }
        return major + "." + minor;
    }

    @Override
    public Optional<ScriptCompilerSpec> getCompilerSpecWithVersion(String compilerName, Integer major, Integer min) {
        return Optional.ofNullable(compilerSpecs.get(makeSpecKey(compilerName, versionString(major, min))));
    }

    @Override
    public Collection<ScriptCompilerSpec> getAllCompilerSpec() {
        return compilerSpecs.values();
    }

    /**
     * 导入编译器规范
     */
    protected abstract void importCompilerSpecs();

    @Override
    public void startUp() throws RuntimeException {
        importCompilerSpecs();
    }

    @Override
    public void registerCompilerSpec(@NotNull IScriptCompilerMeta meta, @NotNull IScriptCompiler compiler,
        @NotNull IScriptContextFactory contextFactory) {
        AssertUtil.notNull(meta, "compilerSpec can not be null");
        AssertUtil.notNull(compiler, "compiler can not be null");
        if (contextFactory == null) {
            contextFactory = new EmptySimpleScriptContextFactory();
        }
        String compilerSpecKey =
            makeSpecKey(meta.getCompilerName(), versionString(meta.getMajorVersion(), meta.getMinorVersion()));
        ScriptCompilerSpec spec = new ScriptCompilerSpec(compiler, meta, contextFactory);
        if (compilerSpecs.containsKey(compilerSpecKey)) {
            log.warn("compiler spec {} already registered", compilerSpecKey);
            compilerSpecs.put(compilerSpecKey, spec);
            return;
        }
        compilerSpecs.put(compilerSpecKey, spec);
    }
}
