package com.tbs.dic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据字典项
 *
 * @author abstergo
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DicItemModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String label;
    private String value;
    private String remark;
    private Integer flag;
}
