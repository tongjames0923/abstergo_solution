package com.tbs.shared.pojo;

import com.tbs.shared.pojo.audit.time.IDateTimeAuditable;

import java.io.Serializable;

/**
 * 实体基类
 *
 * @param <K>
 * @author tongj
 */
public interface IBaseEntity<K> extends IIdentifiable<K>, IFlagable, IDateTimeAuditable, Serializable {

}
