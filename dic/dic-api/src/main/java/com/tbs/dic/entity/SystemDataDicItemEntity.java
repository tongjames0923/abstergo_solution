package com.tbs.dic.entity;

import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.Date;

@Entity
@Table(name = "system_data_dic_item",
    indexes = {@Index(name = "idx_system_data_dic_item_dic_code", columnList = "dic_code"),
        @Index(name = "idx_flag", columnList = "flag")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Comment("数据字典项表")
@LogicDeleteEntity(activeFlagValues = {1, -1})
public class SystemDataDicItemEntity implements IBaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Comment("主键ID（雪花算法生成）")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 所属数据字典编码
     */
    @Comment("所属数据字典编码")
    @Column(name = "dic_code")
    private String dicCode;

    /**
     * 字典项显示标签
     */
    @Comment("字典项显示标签（展示给用户的名称）")
    @Column(name = "label")
    private String label;

    /**
     * 字典项取值
     */
    @Comment("字典项取值（业务存储与比较的实际值）")
    @Column(name = "value")
    private String value;

    /**
     * 备注
     */
    @Comment("备注")
    @Column(name = "remark")
    private String remark;

    /**
     * 排序号
     */
    @Comment("排序号（升序）")
    @Column(name = "sort")
    private Integer sort;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @Column(name = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @Comment("修改时间")
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 数据字典项状态
     * <p>
     *
     * @see com.tbs.dic.enums.DataDicFlagEnum
     *     </p>
     */
    @Comment("数据字典项状态：1-启用，-1-停用，0-删除")
    @Column(name = "flag")
    private Integer flag;
}
