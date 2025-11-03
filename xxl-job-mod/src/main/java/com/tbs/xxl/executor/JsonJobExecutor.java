package com.tbs.xxl.executor;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.IStartup;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.xxl.api.IJsonJobHandler;
import com.tbs.xxl.model.ExecuteInfo;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author abstergo
 */
@Slf4j
public class JsonJobExecutor implements IStartup {

    Map<String, IJsonJobHandler> jsonJobHandlerMap = new HashMap<>();

    @XxlJob("jsonJobHandler")
    public void function() {
        try {
            final ExecuteInfo executeInfo = JSON.parseObject(XxlJobHelper.getJobParam(), ExecuteInfo.class);
            if (!this.jsonJobHandlerMap.containsKey(executeInfo.getMethod())) {
                XxlJobHelper.handleFail("can not find any method name as " + executeInfo.getMethod());
            } else {
                final IJsonJobHandler jsonJobHandler = this.jsonJobHandlerMap.get(executeInfo.getMethod());
                if (null == jsonJobHandler) {
                    XxlJobHelper.handleFail("no json Job handler");
                    return;
                }
                XxlJobHelper.handleSuccess(jsonJobHandler.handle(jsonJobHandler.paramConvert(executeInfo.getParams())));
            }

        } catch (final Exception e) {
            log.error("xxl-job error", e);
            XxlJobHelper.handleFail("error:" + e.getMessage());
        }
    }

    @XxlJob("available")
    public void availables() {
        final List<Map> ls = new LinkedList<>();
        for (final Map.Entry<String, IJsonJobHandler> entry : this.jsonJobHandlerMap.entrySet()) {
            final Map<String, String> map = new HashMap<>();
            map.put("methodName", entry.getKey());
            map.put("helpText", entry.getValue().help());
            ls.add(map);
        }
        XxlJobHelper.handleSuccess(JSON.toJSONString(ls));
    }

    @XxlJob("help_json")
    public void help() {
        final ExecuteInfo executeInfo = new ExecuteInfo();
        executeInfo.setMethod("demoMethodName");
        final Map m = new HashMap();
        m.put("intType", 1);
        m.put("stringType", "can be anything you need");
        executeInfo.setParams(m);
        XxlJobHelper.handleSuccess(JSON.toJSONString(executeInfo));
    }

    @Override
    public void startUp() throws RuntimeException {
        log.info("json job executor start up");
        try {
            this.jsonJobHandlerMap = SpringUtil.getBeansOfType(IJsonJobHandler.class);
            log.info("json job handler size: {}", this.jsonJobHandlerMap.size());
            for (final Map.Entry<String, IJsonJobHandler> entry : this.jsonJobHandlerMap.entrySet()) {
                log.info("json job handler: {}", entry.getKey());
            }
        } catch (Exception e) {
            log.warn("get json job handler error", e);
        }

    }
}
