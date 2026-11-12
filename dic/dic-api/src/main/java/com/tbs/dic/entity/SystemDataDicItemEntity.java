package com.tbs.dic.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "system_data_dic_item",
    indexes = {@Index(name = "idx_system_data_dic_item_dic_code", columnList = "dic_code"),
        @Index(name = "idx_flag", columnList = "flag")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(activeFlagValues = {1, -1})
public class SystemDataDicItemEntity implements IBaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 所属数据字典编码
     */
    @Column(name = "dic_code")
    private String dicCode;

    /**
     * 字典项显示标签
     */
    @Column(name = "label")
    private String label;

    /**
     * 字典项取值
     */
    @Column(name = "value")
    private String value;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 排序号
     */
    @Column(name = "sort")
    private Integer sort;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 数据字典项状态
     * <p>
     *
     * @see com.tbs.dic.enums.DataDicFlagEnum
     *     </p>
     */
    @Column(name = "flag")
    private Integer flag;
}
