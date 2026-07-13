package com.tbs.shared.pojo;

import java.io.Serializable;

/**
 * 实体基类
 *
 * @param <K>
 * @author tongj
 */
public interface IBaseEntity<K> extends IIdentifiable<K>, IFlagable, Serializable {

}
