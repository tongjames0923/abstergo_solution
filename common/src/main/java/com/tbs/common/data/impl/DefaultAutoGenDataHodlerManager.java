package com.tbs.common.data.impl;

import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderFeatureWrapper;
import com.tbs.common.data.IMatchKeyFounder;
import com.tbs.common.error.AssertUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * @author abstergo
 */
public class DefaultAutoGenDataHodlerManager extends AbstractAutoGenDataHolderManager {

    @Override
    protected IDataHolder getPartOfDataHolder(String name, IDataHolder gobalDataHolder) {
        return new PartitionDataHolder(name, gobalDataHolder);
    }

    /**
     * 分区数据缓存
     */
    public static class PartitionDataHolder implements IDataHolderFeatureWrapper {

        @Getter
        @Setter
        private String partitionName;

        private IDataHolder dataHolder;

        public PartitionDataHolder(String partitionName, IDataHolder dataHolder) {
            this.partitionName = partitionName;
            this.dataHolder = dataHolder;
        }

        private IMatchKeyFounder getMatch() {
            AssertUtil.isTrue(!(getOrginal() instanceof IMatchKeyFounder),
                () -> new RuntimeException("数据缓存不支持匹配键"));
            return (IMatchKeyFounder)dataHolder;
        }

        private String keyGen(String key) {
            return partitionName + "::" + key;
        }

        @Override
        public boolean set(String key, Object value, boolean force) {
            return dataHolder.set(keyGen(key), value, force);
        }

        @Override
        public <T> Optional<T> get(String key) {
            return getOrginal().get(keyGen(key));
        }

        @Override
        public void remove(String key) {
            getOrginal().remove(keyGen(key));
        }

        @Override
        public void clear() {
            getMatch().find(keyGen("*")).forEach(key -> dataHolder.remove(key));
        }

        @Override
        public boolean isEmpty() {
            return getMatch().find(keyGen("*")).isEmpty();
        }

        @Override
        public int size() {
            return getMatch().find(keyGen("*")).size();
        }

        @Override
        public boolean containsKey(String key) {
            return getOrginal().containsKey(keyGen(key));
        }

        @Override
        public Collection<Map.Entry<String, Object>> entries() {
            return getMatch().findEntries(keyGen("*"));
        }

        @Override
        public Collection<String> keys() {
            return getMatch().find(keyGen("*"));
        }

        @Override
        public Collection<Object> values() {
            return getMatch().findValues(keyGen("*"));
        }

        @Override
        public IDataHolder getOrginal() {
            return dataHolder;
        }

        @Override
        public void setOrginal(IDataHolder orginal) {
            this.dataHolder = orginal;
        }

        @Override
        public String reKey(String k) {
            return keyGen(k);
        }
    }
}
