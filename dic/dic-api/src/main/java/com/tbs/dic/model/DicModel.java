package com.tbs.dic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 数据字典模型
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DicModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private String dicCode;
    private String dicName;
    private String remark;
    private Integer flag;
    private List<DicItemModel> items;
}
