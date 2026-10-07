package com.tbs.app.service.impl.push;

import com.tbs.app.constant.SystemI18nConstant;
import com.tbs.app.enums.PushMessageOperationTypeEnum;
import com.tbs.app.form.PushMessageOperateForm;
import com.tbs.app.form.PushMessageQueryForm;
import com.tbs.app.form.PushNotifySendForm;
import com.tbs.app.service.push.INotifyReceiverResolver;
import com.tbs.app.service.push.IPushMessageInterceptor;
import com.tbs.app.service.push.IPushNotifyService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.vo.PushMessageChainVO;
import com.tbs.app.vo.PushMessagePageVO;
import com.tbs.app.vo.PushNotifyMessageVO;
import com.tbs.app.vo.PushSendResultVO;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.push.api.transport.IPushEndpoint;
import com.tbs.shared.mapper.push.PushMessageMapper;
import com.tbs.shared.mapper.push.PushMessageOperationLogMapper;
import com.tbs.shared.mapper.push.PushMessageReceiverMapper;
import com.tbs.shared.pojo.entity.push.PushMessageEntity;
import com.tbs.shared.pojo.entity.push.PushMessageOperationLogEntity;
import com.tbs.shared.pojo.entity.push.PushMessageReceiverEntity;
import com.tbs.sql.service.QueryEngine;
import com.tbs.sql.util.TransactionUtil;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 推送消息中心服务实现
 * <p>
 * 信令+拉取链路的业务实现。数据完整性约定（设计文档 D12/D13）： 数据库三表零业务约束，唯一性与幂等全部由本类写入路径保证—— 消息ID由DB自增生成、fan-out 去重在应用层完成、已读由 READ
 * 日志派生（append-only 天然幂等）。 发送采用编程式事务（TransactionTemplate）：主表+fan-out+SEND 日志原子落库， 事务提交后才发布 SIGNAL
 * 信令（信令失败或推送未启用仅记日志，不影响成功语义）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class PushNotifyServiceImpl implements IPushNotifyService {

    /**
     * 信令频道主题前缀（保留命名空间，业务 topic 不得占用）
     */
    private static final String CHANNEL_TOPIC_PREFIX = "tbs.push.channel.";

    /**
     * 默认每页条数
     */
    private static final int DEFAULT_PAGE_LIMIT = 20;

    /**
     * 单页条数上限
     */
    private static final int MAX_PAGE_LIMIT = 100;

    /**
     * 已读回传单次上限
     */
    private static final int MAX_READ_BATCH = 100;

    /**
     * 清理单批行数
     */
    private static final int CLEANUP_BATCH = 5000;

    /**
     * 未读列表扫描批次上限（防病态全箱扫描）
     */
    private static final int UNREAD_SCAN_MAX_BATCH = 50;

    /**
     * 默认优先级
     */
    private static final String DEFAULT_PRIORITY = PushNotifySendForm.PRIORITY_NORMAL;

    /**
     * 默认业务主题
     */
    private static final String DEFAULT_TOPIC = "global.broadcast";

    /**
     * 默认来源通道
     */
    private static final String DEFAULT_SOURCE = "http";

    /**
     * 推送消息主表 Mapper
     */
    @Resource
    private PushMessageMapper pushMessageMapper;

    /**
     * 消息拦截器列表（SPI 链式分派）
     */
    @Resource
    private ObjectProvider<IPushMessageInterceptor> pushMessageInterceptorProviders;

    /**
     * 推送消息接收人表 Mapper
     */
    @Resource
    private PushMessageReceiverMapper pushMessageReceiverMapper;

    /**
     * 消息操作日志表 Mapper
     */
    @Resource
    private PushMessageOperationLogMapper pushMessageOperationLogMapper;

    /**
     * 系统用户服务（当前登录用户名到用户ID的解析）
     */
    @Resource
    private ISystemUserService systemUserService;

    /**
     * 事务管理器（编程式事务模板由其构建）
     */
    @Resource
    private PlatformTransactionManager transactionManager;

    /**
     * 接收人解析器列表（SPI 链式分派，业务实现 + 默认兜底）
     */
    @Autowired
    private List<INotifyReceiverResolver> receiverResolvers;

    /**
     * 推送门面延迟获取（tbs.push.enable 未开启时缺失，信令发布降级为仅落库）
     */
    @Autowired
    private ObjectProvider<IPushEndpoint> pushEndpointProvider;

    /**
     * 发送持久化通知（事务内三表原子落库，提交后发信令）
     *
     * @param form 发送表单
     * @return 发送结果视图（messageId/topic/msgType/receiverCount/signalCount）
     */
    @Override
    public PushSendResultVO send(PushNotifySendForm form) {
        validateSendForm(form);
        for (IPushMessageInterceptor interceptor : pushMessageInterceptorProviders) {
            if (!interceptor.support(form)) {
                continue;
            }
            form = interceptor.intercept(form);
        }
        Date now = new Date();
        List<Long> receivers = resolveReceivers(form);
        PushMessageEntity entity = buildMessageEntity(form, now);
        PushNotifySendForm finalForm = form;
        TransactionUtil.getInstance().executeTransaction(() -> {
            doSend(entity, receivers, finalForm, now);
        });
        int signalCount = publishSignals(receivers);
        log.info("推送通知发送完成, messageId={}, topic={}, receiverCount={}, signalCount={}", entity.getId(),
            entity.getTopic(), receivers.size(), signalCount);
        return new PushSendResultVO(entity.getId(), entity.getTopic(), entity.getMsgType(), receivers.size(),
            signalCount);
    }

    /**
     * 分页拉取当前用户消息列表（到达序倒序，含已读）
     *
     * @param form 查询表单
     * @return 分页结果
     */
    @Override
    public PushMessagePageVO fetchMessages(PushMessageQueryForm form) {
        PushMessageQueryForm query = form == null ? new PushMessageQueryForm() : form;
        Long receiverId = currentUserId();
        int limit = resolvePageLimit(query.getLimit());
        String readFlag = resolveReadFlag(query.getReadFlag());
        boolean unreadOnly = PushMessageQueryForm.READ_FLAG_UNREAD.equals(readFlag);

        List<PushMessageReceiverEntity> collected =
            collectRelationRows(receiverId, query.getCursor(), limit, unreadOnly);
        boolean hasMore = collected.size() > limit;
        List<PushMessageReceiverEntity> pageRows = hasMore ? collected.subList(0, limit) : collected;

        Map<Long, Date> readTimes = fetchReadTimes(receiverId, messageIdsOf(pageRows));
        Map<Long, PushMessageEntity> messageMap = fetchMessageMap(messageIdsOf(pageRows));

        List<PushNotifyMessageVO> vos = new ArrayList<>();
        Long cursor = null;
        for (PushMessageReceiverEntity row : pageRows) {
            PushMessageEntity message = messageMap.get(row.getMessageId());
            if (message == null) {
                continue;
            }
            Date readTime = readTimes.get(row.getMessageId());
            vos.add(toMessageVO(message, readTime != null, readTime));
            cursor = row.getId();
        }
        return new PushMessagePageVO(vos, cursor, hasMore);
    }

    /**
     * 统计当前用户未读数（接收关系行数 − 已读消息DISTINCT数）
     *
     * @return 未读数
     */
    @Override
    public long unreadCount() {
        Long receiverId = currentUserId();
        long total = pushMessageReceiverMapper.selectCount(receiverProbe(receiverId));
        long readDistinct = distinctReadCount(receiverId);
        return Math.max(0L, total - readDistinct);
    }

    /**
     * 批量记录已读（写 READ 操作日志，幂等）
     *
     * @param messageIds 消息ID列表
     * @return 实际记录条数
     */
    @Override
    public int markRead(List<Long> messageIds) {
        AssertUtil.isTrue(CollUtil.isEmpty(messageIds)).i18n(SystemI18nConstant.PUSH_MESSAGE_READ_IDS_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(messageIds.size() > MAX_READ_BATCH).i18n(SystemI18nConstant.PUSH_MESSAGE_READ_IDS_TOO_MANY)
            .params(MAX_READ_BATCH).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        Long operator = currentUserId();
        Date now = new Date();
        Set<Long> ownedIds = ownedMessageIds(operator, messageIds);
        Set<Long> alreadyRead = fetchReadTimes(operator, new ArrayList<>(ownedIds)).keySet();
        List<Long> toRecord = messageIds.stream().filter(id -> ownedIds.contains(id) && !alreadyRead.contains(id))
            .collect(Collectors.toList());
        TransactionUtil.getInstance().executeTransaction(() -> {
            for (Long messageId : toRecord) {
                pushMessageOperationLogMapper.insert(
                    buildOperationLog(messageId, operator, PushMessageOperationTypeEnum.READ, DEFAULT_SOURCE, now,
                        null));
            }

        });
        return toRecord.size();
    }

    /**
     * 通用操作回传（CLICK 等，CLICK 默认同时补记 READ）
     *
     * @param form 操作回传表单
     * @return 实际写入日志条数
     */
    @Override
    public int recordOperation(PushMessageOperateForm form) {
        AssertUtil.isTrue(form == null || form.getMessageId() == null)
            .i18n(SystemI18nConstant.PUSH_MESSAGE_OP_MESSAGE_REQUIRED).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        if (PushMessageOperationTypeEnum.READ.getValue().equals(form.getOpType())) {
            return markRead(Collections.singletonList(form.getMessageId()));
        }
        AssertUtil.isTrue(!PushMessageOperationTypeEnum.isSupported(form.getOpType()))
            .i18n(SystemI18nConstant.PUSH_MESSAGE_OP_TYPE_INVALID).params(form.getOpType())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        Long operator = currentUserId();
        Date now = new Date();
        if (!ownedMessageIds(operator, Collections.singletonList(form.getMessageId())).contains(form.getMessageId())) {
            return 0;
        }
        TransactionUtil.getInstance().executeTransaction(() -> {
            pushMessageOperationLogMapper.insert(buildOperationLog(form.getMessageId(), operator,
                PushMessageOperationTypeEnum.fromValue(form.getOpType()), DEFAULT_SOURCE, now, form.getExtInfo()));
            if (PushMessageOperationTypeEnum.CLICK.getValue().equals(form.getOpType()) &&
                !fetchReadTimes(operator, Collections.singletonList(form.getMessageId())).containsKey(
                    form.getMessageId())) {
                pushMessageOperationLogMapper.insert(
                    buildOperationLog(form.getMessageId(), operator, PushMessageOperationTypeEnum.READ, DEFAULT_SOURCE,
                        now, null));
            }
        });
        return PushMessageOperationTypeEnum.CLICK.getValue().equals(form.getOpType()) ? 2 : 1;
    }

    /**
     * 查询单消息全链路（SEND 事件 + 投递关系 + 处理事件拼图）
     *
     * @param messageId 消息ID
     * @return 全链路视图
     */
    @Override
    public PushMessageChainVO queryChain(Long messageId) {
        AssertUtil.isTrue(messageId == null).i18n(SystemI18nConstant.PUSH_MESSAGE_OP_MESSAGE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        PushMessageEntity message = pushMessageMapper.selectByPrimaryKey(messageId);
        AssertUtil.isTrue(message == null).i18n(SystemI18nConstant.PUSH_MESSAGE_NOT_EXISTS).params(messageId)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        List<PushMessageOperationLogEntity> logs =
            QueryEngine.getEntityQueryWrapper(PushMessageOperationLogEntity.class).query().where()
                .equal(PushMessageOperationLogEntity::getMessageId, messageId).pour().fetch();
        List<PushMessageReceiverEntity> relations =
            QueryEngine.getEntityQueryWrapper(PushMessageReceiverEntity.class).query().where()
                .equal(PushMessageReceiverEntity::getMessageId, messageId).pour().fetch();
        PushMessageChainVO chain = new PushMessageChainVO();
        chain.setMessage(toMessageVO(message, null, null));
        chain.setSendEvent(
            logs.stream().filter(l -> PushMessageOperationTypeEnum.SEND.getValue().equals(l.getOpType())).findFirst()
                .map(this::toOperationVO).orElse(null));
        chain.setReceivers(relations.stream().map(PushMessageReceiverEntity::getReceiver).collect(Collectors.toList()));
        chain.setOperations(
            logs.stream().filter(l -> !PushMessageOperationTypeEnum.SEND.getValue().equals(l.getOpType()))
                .map(this::toOperationVO).collect(Collectors.toList()));
        return chain;
    }

    /**
     * 清理截止时间之前的数据（三表物理删除，分批执行）
     *
     * @param deadline 截止时间
     * @return 清理总行数
     */
    @Override
    public int cleanupBefore(Date deadline) {
        AssertUtil.isTrue(deadline == null).i18n(SystemI18nConstant.PUSH_MESSAGE_OP_MESSAGE_REQUIRED)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        int total = cleanupPushMessage(deadline) + cleanupReceiver(deadline) + cleanupOperationLog(deadline);
        log.info("推送消息数据清理完成, deadline={}, total={}", deadline, total);
        return total;
    }

    /**
     * 发送事务体：主表+fan-out+SEND日志原子写入
     *
     * @param entity    消息实体
     * @param receivers 去重后的接收人列表
     * @param form      发送表单
     * @param now       当前时间
     */
    private void doSend(PushMessageEntity entity, List<Long> receivers, PushNotifySendForm form, Date now) {
        pushMessageMapper.insert(entity);
        if (!receivers.isEmpty()) {
            List<PushMessageReceiverEntity> rows = new ArrayList<>();
            for (Long receiver : receivers) {
                PushMessageReceiverEntity row = new PushMessageReceiverEntity();
                row.setMessageId(entity.getId());
                row.setReceiver(receiver);
                row.setCreateTime(now);
                row.setUpdateTime(now);
                rows.add(row);
            }
            pushMessageReceiverMapper.insertList(rows);
        }
        pushMessageOperationLogMapper.insert(
            buildOperationLog(entity.getId(), resolveSendOperator(), PushMessageOperationTypeEnum.SEND,
                resolveSource(form.getSource()), now, form.getExtInfo()));
    }

    /**
     * 发送表单契约校验
     *
     * @param form 发送表单
     */
    private void validateSendForm(PushNotifySendForm form) {
        AssertUtil.isTrue(form == null).i18n(SystemI18nConstant.PUSH_NOTIFY_FORM_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(form.getMsgType())).i18n(SystemI18nConstant.PUSH_NOTIFY_TYPE_REQUIRED)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(form.getMsgType().length() > 32).i18n(SystemI18nConstant.PUSH_NOTIFY_TYPE_TOO_LONG)
            .params(form.getMsgType()).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(form.getTitle() != null && form.getTitle().length() > 255)
            .i18n(SystemI18nConstant.PUSH_NOTIFY_TITLE_TOO_LONG).params(255).errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();
        AssertUtil.isTrue(StrUtil.isNotEmpty(form.getPriority()) &&
                !PushNotifySendForm.SUPPORTED_PRIORITIES.contains(form.getPriority()))
            .i18n(SystemI18nConstant.PUSH_NOTIFY_PRIORITY_INVALID).params(form.getPriority())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(form.getTopic() != null && form.getTopic().length() > 128)
            .i18n(SystemI18nConstant.PUSH_NOTIFY_TOPIC_TOO_LONG).params(form.getTopic())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(form.getUrl() != null && form.getUrl().length() > 512)
            .i18n(SystemI18nConstant.PUSH_NOTIFY_URL_TOO_LONG).params(form.getUrl())
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
    }

    /**
     * SPI 链式分派解析接收人并去重
     *
     * @param form 发送表单
     * @return 去重后的接收人列表
     */
    private List<Long> resolveReceivers(PushNotifySendForm form) {
        Set<Long> deduped = new LinkedHashSet<>();
        for (INotifyReceiverResolver resolver : receiverResolvers) {
            if (!resolver.supports(form)) {
                continue;
            }
            List<Long> resolved = resolver.resolveReceivers(form);
            if (CollUtil.isNotEmpty(resolved)) {
                deduped.addAll(resolved);
            }
        }
        return new ArrayList<>(deduped);
    }

    /**
     * 事务提交后逐接收人发布 SIGNAL 信令（推送未启用或单条失败仅记日志）
     *
     * @param receivers 接收人列表
     * @return 成功发布的信令条数
     */
    private int publishSignals(List<Long> receivers) {
        IPushEndpoint endpoint = pushEndpointProvider.getIfAvailable();
        if (endpoint == null) {
            log.warn("推送能力未启用(tbs.push.enable=false)，信令发布跳过, receiverCount={}", receivers.size());
            return 0;
        }
        int published = 0;
        for (Long receiver : receivers) {
            try {
                endpoint.publishSignal(CHANNEL_TOPIC_PREFIX + receiver, null);
                published++;
            } catch (Exception error) {
                log.warn("SIGNAL信令发布失败, receiver={}", receiver, error);
            }
        }
        return published;
    }

    /**
     * 构造消息本体实体
     *
     * @param form 发送表单
     * @param now  当前时间
     * @return 消息实体
     */
    private PushMessageEntity buildMessageEntity(PushNotifySendForm form, Date now) {
        PushMessageEntity entity = new PushMessageEntity();
        entity.setTopic(StrUtil.isEmpty(form.getTopic()) ? DEFAULT_TOPIC : form.getTopic());
        entity.setMsgType(form.getMsgType());
        entity.setTitle(form.getTitle());
        entity.setContent(form.getContent());
        entity.setPriority(StrUtil.isEmpty(form.getPriority()) ? DEFAULT_PRIORITY : form.getPriority());
        entity.setSender(StrUtil.isEmpty(form.getSender()) ? resolveSendOperatorName() : form.getSender());
        entity.setUrl(form.getUrl());
        entity.setExtInfo(form.getExtInfo() == null ? null : JsonUtils.toJSONString(form.getExtInfo()));
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        return entity;
    }

    /**
     * 构造操作日志实体
     *
     * @param messageId 消息ID
     * @param operator  操作人（可空）
     * @param opType    操作类型
     * @param source    来源通道
     * @param now       当前时间
     * @param extInfo   上下文扩展（对象或字符串）
     * @return 日志实体
     */
    private PushMessageOperationLogEntity buildOperationLog(Long messageId, Long operator,
        PushMessageOperationTypeEnum opType, String source, Date now, Object extInfo) {
        PushMessageOperationLogEntity entity = new PushMessageOperationLogEntity();
        entity.setMessageId(messageId);
        entity.setOperator(operator);
        entity.setOpType(opType.getValue());
        entity.setOpSource(source);
        entity.setOpTime(now);
        if (extInfo instanceof String) {
            entity.setExtInfo((String)extInfo);
        } else if (extInfo != null) {
            entity.setExtInfo(JsonUtils.toJSONString(extInfo));
        }
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        return entity;
    }

    /**
     * 解析 SEND 日志操作人（无认证上下文时返回null，身份经 op_source/ext_info 审计）
     *
     * @return 操作人ID，可空
     */
    private Long resolveSendOperator() {
        try {
            List<Long> ids =
                systemUserService.queryUserIdsByUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
            return CollUtil.getFirst(ids);
        } catch (Exception error) {
            return null;
        }
    }

    /**
     * 解析发送者展示标识缺省值（取当前登录用户名，无认证上下文时回退 system）
     *
     * @return 发送者展示标识
     */
    private String resolveSendOperatorName() {
        try {
            return AuthUtil.getInstance().getCurrentUser().getUsername();
        } catch (Exception error) {
            return "system";
        }
    }

    /**
     * 解析来源通道（缺省http）
     *
     * @param source 表单声明的来源
     * @return 来源通道
     */
    private String resolveSource(String source) {
        return StrUtil.isEmpty(source) ? DEFAULT_SOURCE : source;
    }

    /**
     * 获取当前登录用户ID（严格模式，取不到抛系统错误）
     *
     * @return 当前用户ID
     */
    private Long currentUserId() {
        List<Long> ids;
        try {
            ids = systemUserService.queryUserIdsByUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
        } catch (Exception error) {
            ids = Collections.emptyList();
        }
        AssertUtil.isTrue(CollUtil.isEmpty(ids)).i18n(SystemI18nConstant.PUSH_MESSAGE_USER_NOT_FOUND)
            .errorLevel(ErrorLevelEnum.SYSTEM_ERROR).throwIt();
        return CollUtil.getFirst(ids);
    }

    /**
     * 游标分页收集接收关系行（到达序倒序；unreadOnly 模式下边扫描边派生已读过滤）
     *
     * @param receiverId 接收用户ID
     * @param cursor     游标（可空）
     * @param limit      页大小
     * @param unreadOnly 是否仅未读
     * @return 收集的关系行（可能为 limit+1 行，hasMore 由调用方判定）
     */
    private List<PushMessageReceiverEntity> collectRelationRows(Long receiverId, Long cursor, int limit,
        boolean unreadOnly) {
        List<PushMessageReceiverEntity> collected = new ArrayList<>();
        Long cursorInner = cursor;
        boolean exhausted = false;
        int scanBatches = 0;
        while (collected.size() < limit + 1 && !exhausted) {
            List<PushMessageReceiverEntity> batch = fetchRelationRows(receiverId, cursorInner, limit + 1);
            if (batch.isEmpty()) {
                break;
            }
            exhausted = batch.size() < limit + 1;
            if (!unreadOnly) {
                collected.addAll(batch);
            } else {
                Map<Long, Date> readTimes = fetchReadTimes(receiverId, messageIdsOf(batch));
                for (PushMessageReceiverEntity row : batch) {
                    if (!readTimes.containsKey(row.getMessageId())) {
                        collected.add(row);
                    }
                }
            }
            if (!exhausted) {
                cursorInner = batch.get(batch.size() - 1).getId();
            }
            if (++scanBatches >= UNREAD_SCAN_MAX_BATCH) {
                break;
            }
        }
        return collected;
    }

    /**
     * 按游标拉取一批接收关系行（到达序倒序）
     *
     * @param receiverId 接收用户ID
     * @param cursor     游标（可空）
     * @param fetchCount 拉取行数
     * @return 关系行列表
     */
    private List<PushMessageReceiverEntity> fetchRelationRows(Long receiverId, Long cursor, int fetchCount) {
        var qb = QueryEngine.getEntityQueryWrapper(PushMessageReceiverEntity.class).query();
        if (cursor == null) {
            qb.where().equal(PushMessageReceiverEntity::getReceiver, receiverId).pour();
        } else {
            qb.where().equal(PushMessageReceiverEntity::getReceiver, receiverId)
                .lessThan(PushMessageReceiverEntity::getId, cursor).pour();
        }
        qb.getOrderSupport().ifPresent(order -> order.orderByDesc(PushMessageReceiverEntity::getId));
        qb.getPageLimitSupport().ifPresent(page -> page.limit(0L, (long)fetchCount));
        return qb.fetch();
    }

    /**
     * 批量派生指定消息在当前用户视角下的首次已读时间
     *
     * @param operatorId 操作人ID
     * @param messageIds 消息ID列表
     * @return messageId → 首次已读时间
     */
    private Map<Long, Date> fetchReadTimes(Long operatorId, List<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return Collections.emptyMap();
        }
        List<PushMessageOperationLogEntity> logs =
            QueryEngine.getEntityQueryWrapper(PushMessageOperationLogEntity.class).query().where()
                .equal(PushMessageOperationLogEntity::getOperator, operatorId)
                .in(PushMessageOperationLogEntity::getMessageId, messageIds)
                .equal(PushMessageOperationLogEntity::getOpType, PushMessageOperationTypeEnum.READ.getValue()).pour()
                .fetch();
        Map<Long, Date> result = new HashMap<>();
        for (PushMessageOperationLogEntity logEntity : logs) {
            result.merge(logEntity.getMessageId(), logEntity.getOpTime(),
                (first, second) -> first.before(second) ? first : second);
        }
        return result;
    }

    /**
     * 批量获取消息本体并建立ID索引
     *
     * @param messageIds 消息ID列表
     * @return messageId → 消息实体
     */
    private Map<Long, PushMessageEntity> fetchMessageMap(List<Long> messageIds) {
        if (CollUtil.isEmpty(messageIds)) {
            return Collections.emptyMap();
        }
        return pushMessageMapper.selectByIdList(messageIds).stream()
            .collect(Collectors.toMap(PushMessageEntity::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * 校验当前用户对接收关系的所有权并返回有效消息ID集合
     *
     * @param operatorId 操作人ID
     * @param messageIds 消息ID列表
     * @return 属于当前用户的消息ID集合
     */
    private Set<Long> ownedMessageIds(Long operatorId, List<Long> messageIds) {
        List<PushMessageReceiverEntity> rows =
            QueryEngine.getEntityQueryWrapper(PushMessageReceiverEntity.class).query().where()
                .equal(PushMessageReceiverEntity::getReceiver, operatorId)
                .in(PushMessageReceiverEntity::getMessageId, messageIds).pour().fetch();
        return rows.stream().map(PushMessageReceiverEntity::getMessageId).collect(Collectors.toSet());
    }

    /**
     * 统计用户已读消息DISTINCT数（重复READ日志消化）
     *
     * @param operatorId 操作人ID
     * @return DISTINCT已读消息数
     */
    private long distinctReadCount(Long operatorId) {
        List<PushMessageOperationLogEntity> logs =
            QueryEngine.getEntityQueryWrapper(PushMessageOperationLogEntity.class).query().where()
                .equal(PushMessageOperationLogEntity::getOperator, operatorId)
                .equal(PushMessageOperationLogEntity::getOpType, PushMessageOperationTypeEnum.READ.getValue()).pour()
                .fetch();
        return logs.stream().map(PushMessageOperationLogEntity::getMessageId).distinct().count();
    }

    /**
     * 构造接收关系探针（tk.mybatis selectCount 按非空字段计数）
     *
     * @param receiverId 接收用户ID
     * @return 探针实体
     */
    private PushMessageReceiverEntity receiverProbe(Long receiverId) {
        PushMessageReceiverEntity probe = new PushMessageReceiverEntity();
        probe.setReceiver(receiverId);
        return probe;
    }

    /**
     * 提取关系行的消息ID列表
     *
     * @param rows 关系行列表
     * @return 消息ID列表
     */
    private List<Long> messageIdsOf(List<PushMessageReceiverEntity> rows) {
        return rows.stream().map(PushMessageReceiverEntity::getMessageId).collect(Collectors.toList());
    }

    /**
     * 组装消息列表行视图
     *
     * @param message  消息实体
     * @param readFlag 是否已读（null按未读处理）
     * @param readTime 首次已读时间
     * @return 行视图
     */
    private PushNotifyMessageVO toMessageVO(PushMessageEntity message, Boolean readFlag, Date readTime) {
        PushNotifyMessageVO vo = new PushNotifyMessageVO();
        vo.setMessageId(message.getId());
        vo.setTopic(message.getTopic());
        vo.setMsgType(message.getMsgType());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setPriority(message.getPriority());
        vo.setSender(message.getSender());
        vo.setUrl(message.getUrl());
        vo.setCreateTime(message.getCreateTime());
        vo.setReadFlag(Boolean.TRUE.equals(readFlag));
        vo.setReadTime(readTime);
        return vo;
    }

    /**
     * 组装操作行为事件视图
     *
     * @param logEntity 日志实体
     * @return 事件视图
     */
    private PushMessageChainVO.OperationVO toOperationVO(PushMessageOperationLogEntity logEntity) {
        return new PushMessageChainVO.OperationVO(logEntity.getOperator(), logEntity.getOpType(),
            logEntity.getOpSource(), logEntity.getOpTime(), logEntity.getExtInfo());
    }

    /**
     * 解析页大小（缺省20，越界拒绝）
     *
     * @param limit 表单页大小
     * @return 有效页大小
     */
    private int resolvePageLimit(Integer limit) {
        int resolved = limit == null ? DEFAULT_PAGE_LIMIT : limit;
        AssertUtil.isTrue(resolved < 1 || resolved > MAX_PAGE_LIMIT)
            .i18n(SystemI18nConstant.PUSH_MESSAGE_QUERY_LIMIT_INVALID).params(resolved)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return resolved;
    }

    /**
     * 解析读状态筛选标记（缺省all，非法拒绝）
     *
     * @param readFlag 表单标记
     * @return 有效标记
     */
    private String resolveReadFlag(String readFlag) {
        String resolved = StrUtil.isEmpty(readFlag) ? PushMessageQueryForm.READ_FLAG_ALL : readFlag;
        AssertUtil.isTrue(!PushMessageQueryForm.READ_FLAG_ALL.equals(resolved) &&
                !PushMessageQueryForm.READ_FLAG_UNREAD.equals(resolved))
            .i18n(SystemI18nConstant.PUSH_MESSAGE_QUERY_FLAG_INVALID).params(resolved)
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        return resolved;
    }

    /**
     * 清理主表截止数据（按 create_time 走索引分批删除）
     *
     * @param deadline 截止时间
     * @return 清理行数
     */
    private int cleanupPushMessage(Date deadline) {
        int total = 0;
        while (true) {
            var qb = QueryEngine.getEntityQueryWrapper(PushMessageEntity.class).query().where()
                .lessThan(PushMessageEntity::getCreateTime, deadline).pour();
            qb.getPageLimitSupport().ifPresent(page -> page.limit(0L, (long)CLEANUP_BATCH));
            List<Long> ids = qb.fetch().stream().map(PushMessageEntity::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                break;
            }
            pushMessageMapper.deleteByIdList(ids);
            total += ids.size();
        }
        return total;
    }

    /**
     * 清理接收人表截止数据（按 create_time 走索引分批删除）
     *
     * @param deadline 截止时间
     * @return 清理行数
     */
    private int cleanupReceiver(Date deadline) {
        int total = 0;
        while (true) {
            var qb = QueryEngine.getEntityQueryWrapper(PushMessageReceiverEntity.class).query().where()
                .lessThan(PushMessageReceiverEntity::getCreateTime, deadline).pour();
            qb.getPageLimitSupport().ifPresent(page -> page.limit(0L, (long)CLEANUP_BATCH));
            List<Long> ids = qb.fetch().stream().map(PushMessageReceiverEntity::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                break;
            }
            pushMessageReceiverMapper.deleteByIdList(ids);
            total += ids.size();
        }
        return total;
    }

    /**
     * 清理操作日志表截止数据（按 op_time 走 idx_op_time 索引分批删除）
     *
     * @param deadline 截止时间
     * @return 清理行数
     */
    private int cleanupOperationLog(Date deadline) {
        int total = 0;
        while (true) {
            var qb = QueryEngine.getEntityQueryWrapper(PushMessageOperationLogEntity.class).query().where()
                .lessThan(PushMessageOperationLogEntity::getOpTime, deadline).pour();
            qb.getPageLimitSupport().ifPresent(page -> page.limit(0L, (long)CLEANUP_BATCH));
            List<Long> ids = qb.fetch().stream().map(PushMessageOperationLogEntity::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                break;
            }
            pushMessageOperationLogMapper.deleteByIdList(ids);
            total += ids.size();
        }
        return total;
    }

}
