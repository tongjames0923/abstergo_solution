package com.tbs.web.result.impl;

import com.tbs.common.function.SConsumer;
import com.tbs.common.util.CollUtil;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.IAjaxResultBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 默认的ajax结果构造器
 *
 * @author tongj
 */
public class SimpleAjaxResultBuilder implements IAjaxResultBuilder {

    public class AnyThingsAjaxResult1Step implements IAjaxResultBuilder.IAjaxResult1Step {
        private Map<String, Object> map = new HashMap<>(4);

        @Override
        public <T> IAjaxResult1Step appendField(String feild, T value) {
            map.putIfAbsent(feild, value);
            return this;
        }

        /**
         * 添加msg
         *
         * @param msg
         * @return
         */
        public IAjaxResult1Step acceptMsg(String msg) {
            this.appendField(AjaxResult.MESSAGE_KEY, msg);
            return this;
        }

        /**
         * 添加data
         *
         * @param data
         * @return
         */
        public IAjaxResult1Step acceptData(Object data) {
            this.appendField(AjaxResult.DATA_KEY, data);
            return this;
        }

        /**
         * 添加code
         *
         * @param code
         * @return
         */
        public IAjaxResult1Step acceptCode(int code) {
            this.appendField(AjaxResult.CODE_KEY, code);
            return this;
        }

        @Override
        public IAjaxResult2Step next() {
            return new AnyThingsAjaxResult2Step(map);
        }
    }

    public class AnyThingsAjaxResult2Step implements IAjaxResultBuilder.IAjaxResult2Step {
        private AjaxResult ajaxResult = new AjaxResult();

        public AnyThingsAjaxResult2Step(Map<String, Object> datas) {
            if (CollUtil.isEmpty(datas)) {
                return;
            }
            if (ajaxResult.getMap() == null) {
                ajaxResult.setMap(new LinkedHashMap<>(datas.size()));
            }
            ajaxResult.getMap().putAll(datas);
        }

        @Override
        public IAjaxResult2Step preHandle(SConsumer<Map<String, Object>> consumer) {
            if (consumer != null) {
                consumer.accept(ajaxResult.getMap());
            }
            return this;
        }

        @Override
        public AjaxResult build() {
            return ajaxResult;
        }
    }

    @Override
    public IAjaxResult1Step create() {
        return new AnyThingsAjaxResult1Step();
    }
}
