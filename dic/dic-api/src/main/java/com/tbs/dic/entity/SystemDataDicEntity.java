package com.tbs.dic.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sys_data_dic",
    indexes = {@Index(name = "idx_data_dic_name_code", columnList = "data_dic_name,data_dic_code"),
        @Index(name = "idx_flag", columnList = "flag")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(filedName = "flag", activeFlagValues = {1, -1})
@Comment("数据字典主表")
public class SystemDataDicEntity implements IBaseEntity<Long> {

    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Comment("主键ID（自增）")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 数据字典名称
     */
    @Comment("数据字典名称")
    @Column(name = "data_dic_name")
    private String dataDicName;

    /**
     * 数据字典编码
     */
    @Comment("数据字典编码（业务侧按编码检索字典项）")
    @Column(name = "data_dic_code")
    private String dataDicCode;

    /**
     * 状态
     *
     * @see com.tbs.dic.enums.DataDicFlagEnum
     */
    @Comment("数据字典状态：1-启用，-1-停用，0-删除")
    @Column(name = "flag")
    private Integer flag;

    /**
     * 备注
     */
    @Comment("备注")
    @Column(name = "remark")
    private String remark;

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

}
