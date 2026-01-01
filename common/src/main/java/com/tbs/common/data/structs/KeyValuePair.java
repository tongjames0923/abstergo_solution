package com.tbs.common.data.structs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 键值对
 *
 * @author abstergo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValuePair<K, V> implements Serializable {
    private static final long serialVersionUID = 1L;
    private K key;
    private V value;
}
