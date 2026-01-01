package com.tbs.script.api.compiler.impl;

import com.tbs.script.api.compiler.AbstractScriptCompiler;
import com.tbs.script.api.context.IScriptContext;
import com.tbs.script.model.ScriptCompilerFeatures;
import com.tbs.script.model.ScriptResult;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * SpEL脚本编译器
 *
 * @author abstergo
 */
public class SpelCompiler extends AbstractScriptCompiler {
    @Override
    protected IScriptRunner getScriptRunner(String script, ScriptCompilerFeatures features) {
        return new IScriptRunner() {
            @Override
            public ScriptResult execute(IScriptContext scriptContext) {
                SpelExpressionParser parser = new SpelExpressionParser();
                EvaluationContext evaluationContext = new StandardEvaluationContext();
                Object result = null;
                if (scriptContext != null) {
                    for (var localVar : scriptContext.listLocalVariables()) {
                        evaluationContext.setVariable(localVar, scriptContext.getLocalVariable(localVar, Object.class));
                    }
                }
                try {
                    result = parser.parseExpression(script).getValue(evaluationContext);
                } catch (Exception e) {
                    return new ScriptResult(false, null, null, e.getMessage());
                }
                return new ScriptResult(true, result, result != null ? result.getClass() : null, null);
            }
        };
    }

    @Override
    public String getCompilerName() {
        return "SpEL Compiler";
    }

    @Override
    public Integer getMajorVersion() {
        return 1;
    }

    @Override
    public Integer getMinorVersion() {
        return 0;
    }

    @Override
    public String getScriptLanguage() {
        return "SpEL";
    }
}
