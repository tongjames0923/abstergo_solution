package com.tbs.sdk.push;

import com.tbs.sdk.constants.SdkConstants;
import com.tbs.sdk.exception.SdkClientException;
import com.tbs.sdk.http.IBaseHttpClient;
import com.tbs.sdk.model.PushMessageChain;
import com.tbs.sdk.model.PushMessageOperateForm;
import com.tbs.sdk.model.PushMessagePage;
import com.tbs.sdk.model.PushMessageQueryForm;
import com.tbs.sdk.model.PushNotifySendForm;
import com.tbs.sdk.model.PushSendResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 推送消息中心实现
 * <p>
 * 六个端点的强类型封装：参数本地预校验（单次已读上限、查询字段透传），
 * 网络与统一响应解析委托IBaseHttpClient。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public class PushMessageApi implements IPushMessageApi {

    /**
     * 统一HTTP客户端
     */
    private final IBaseHttpClient httpClient;

    /**
     * 构造推送消息中心API
     *
     * @param httpClient 统一HTTP客户端
     */
    public PushMessageApi(IBaseHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PushSendResult notify(PushNotifySendForm form) {
        if (form == null) {
            throw new SdkClientException("发送表单不能为空");
        }
        if (form.getMsgType() == null || form.getMsgType().trim().isEmpty()) {
            throw new SdkClientException("msgType不能为空");
        }
        return httpClient.post(SdkConstants.PATH_PUSH_NOTIFY, form, PushSendResult.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PushMessagePage list(PushMessageQueryForm query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            if (query.getCursor() != null) {
                params.put("cursor", String.valueOf(query.getCursor()));
            }
            if (query.getLimit() != null) {
                params.put("limit", String.valueOf(query.getLimit()));
            }
            if (query.getReadFlag() != null) {
                params.put("readFlag", query.getReadFlag());
            }
        }
        return httpClient.get(SdkConstants.PATH_PUSH_LIST, params, PushMessagePage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long unreadCount() {
        return httpClient.get(SdkConstants.PATH_PUSH_UNREAD_COUNT, null, Long.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer markRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            throw new SdkClientException("messageIds不能为空");
        }
        if (messageIds.size() > SdkConstants.READ_BATCH_MAX) {
            throw new SdkClientException("单次标记已读不能超过" + SdkConstants.READ_BATCH_MAX + "条");
        }
        return httpClient.post(SdkConstants.PATH_PUSH_READ, messageIds, Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer operate(PushMessageOperateForm form) {
        if (form == null || form.getMessageId() == null) {
            throw new SdkClientException("messageId不能为空");
        }
        if (form.getOpType() == null || form.getOpType().trim().isEmpty()) {
            throw new SdkClientException("opType不能为空");
        }
        return httpClient.post(SdkConstants.PATH_PUSH_OPERATE, form, Integer.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PushMessageChain chain(Long messageId) {
        if (messageId == null) {
            throw new SdkClientException("messageId不能为空");
        }
        Map<String, String> params = new HashMap<>();
        params.put("messageId", String.valueOf(messageId));
        return httpClient.get(SdkConstants.PATH_PUSH_CHAIN, params, PushMessageChain.class);
    }
}
