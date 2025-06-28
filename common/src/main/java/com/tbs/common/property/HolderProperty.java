package com.tbs.common.property;

import com.tbs.common.holder.IContextHolder;
import com.tbs.common.holder.impl.ImportedContextHolder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tbs.holder", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class HolderProperty {

    /**
     * 主contextHolder类型
     */
    @Getter
    @Setter
    private Class<? extends IContextHolder> mainContextHolderType = ImportedContextHolder.class;

    /**
     * 是否启用一个默认的本地存储context
     */
    @Getter
    @Setter
    private boolean useLocalStoredContext = false;
    /**
     * 是否启用一个默认的线程存储context
     */
    @Getter
    @Setter
    private boolean useThreadLocalStoredContext = false;

}
