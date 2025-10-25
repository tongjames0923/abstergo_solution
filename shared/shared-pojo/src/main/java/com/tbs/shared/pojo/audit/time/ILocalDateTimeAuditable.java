package com.tbs.shared.pojo.audit.time;

import com.tbs.shared.pojo.audit.ITimeAuditable;

import java.time.LocalDateTime;

/**
 * 本地时间审计接口
 *
 * @author abstergo
 */
public interface ILocalDateTimeAuditable extends ITimeAuditable<LocalDateTime> {
}
