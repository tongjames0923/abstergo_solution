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
@Table(name = "sys_data_dic",
    indexes = {@Index(name = "idx_data_dic_name_code", columnList = "data_dic_name,data_dic_code"),
        @Index(name = "idx_flag", columnList = "flag")})
@Data
@AllArgsConstructor
@NoArgsConstructor
@LogicDeleteEntity(filedName = "flag", activeFlagValues = {1, -1})
public class SystemDataDicEntity implements IBaseEntity<Long> {

    private static final long serialVersionUID = 1L;
    /**
     * 主键
     */
    @Column(name = "id")
    @Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 数据字典名称
     */
    @Column(name = "data_dic_name")
    private String dataDicName;

    /**
     * 数据字典编码
     */
    @Column(name = "data_dic_code")
    private String dataDicCode;

    /**
     * 状态
     *
     * @see com.tbs.dic.enums.DataDicFlagEnum
     */
    @Column(name = "flag")
    private Integer flag;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

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

}
