package com.tbs.xxl.mq;

import com.tbs.common.util.base.CollUtil;
import com.tbs.mq.api.retry.AbstractFailedMessageStore;
import com.tbs.mq.model.FailedMessage;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import java.util.Date;
import java.util.List;

/**
 * xxl job retry
 *
 * @author abstergo
 */

public abstract class AbstractXxlJobRetryExecutor extends AbstractFailedMessageStore {
    
    @XxlJob("mq_fetch_and_retry")
    public void fetchAndRetry() {
        List<FailedMessage> messages = fetchDue(new Date(), 100);
        while (CollUtil.isNotEmpty(messages)) {
            XxlJobHelper.log("fetch and retry messages {}", messages.size());
            for (FailedMessage message : messages) {
                XxlJobHelper.log("fetch and retry message {}", message.getMessageId());
                doRemove(message.getMessageId());
            }
            messages = fetchDue(new Date(), 100);
        }
    }
}
