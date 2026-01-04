package com.tbs.script.api.compiler.impl;

import com.tbs.script.api.compiler.AbstractScriptCompiler;
import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.model.ScriptCompilerFeatures;
import com.tbs.script.model.ScriptResult;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SpEL脚本编译器
 *
 * @author abstergo
 */
public class SpelCompiler extends AbstractScriptCompiler {

    /**
     * 编译器名称 - SpEL编译器的标识名称
     */
    public static final String COMPILER_NAME = "SpEL Compiler";

    /**
     * 脚本结果类型特性 - 脚本结果类型特性标识名称
     */
    public static final String FEATURE_RESULT_TYPE = "resultType";

    /**
     * 主版本号 - 当前编译器的主版本号
     */
    public static final Integer MAJOR_VERSION = 1;
    /**
     * 次版本号 - 当前编译器的次版本号
     */
    public static final Integer MINOR_VERSION = 0;

    /**
     * 脚本语言类型 - 表示该编译器支持的脚本语言为SpEL
     */
    public static final String SCRIPT_LANGUAGE = "SpEL";

    @Override
    protected IScriptRunner getScriptRunner(String script, ScriptCompilerFeatures features) {
        return new IScriptRunner() {
            @Override
            public ScriptResult execute(IScriptContext scriptContext) {
                SpelExpressionParser parser = new SpelExpressionParser(
                    new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, ClassLoader.getSystemClassLoader()));
                EvaluationContext evaluationContext = new StandardEvaluationContext();
                Object result = null;
                if (scriptContext != null) {
                    for (var localVar : scriptContext.listLocalVariables()) {
                        evaluationContext.setVariable(localVar, scriptContext.getLocalVariable(localVar, Object.class));
                    }
                }
                try {
                    Expression ep = parser.parseExpression(script);
                    var resultType = getResultType();
                    if (resultType == null) {
                        result = ep.getValue(evaluationContext);
                    } else {
                        result = ep.getValue(evaluationContext, getResultType());
                    }

                } catch (Exception e) {
                    return new ScriptResult(false, null, null, e.getMessage());
                }
                return new ScriptResult(true, result, result != null ? result.getClass() : null, null);
            }
        };
    }

    @Override
    public String getCompilerName() {
        return COMPILER_NAME;
    }

    public void setResultType(Class<?> resultType) {
        getFeatures().putFeatureCode(FEATURE_RESULT_TYPE, resultType);
    }

    public Class<?> getResultType() {
        return (Class<?>)getFeatures().getFeatureCode(FEATURE_RESULT_TYPE).orElse(null);
    }

    @Override
    public Integer getMajorVersion() {
        return MAJOR_VERSION;
    }

    @Override
    public Integer getMinorVersion() {
        return MINOR_VERSION;
    }

    @Override
    public String getScriptLanguage() {
        return SCRIPT_LANGUAGE;
    }
}
