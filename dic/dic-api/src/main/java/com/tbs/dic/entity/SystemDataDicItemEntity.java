package com.tbs.dic.entity;

import com.tbs.shared.pojo.IBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "system_data_dic_item",
    indexes = {@Index(name = "idx_system_data_dic_item_dic_code", columnList = "dic_code"),
        @Index(name = "idx_flag", columnList = "flag")})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemDataDicItemEntity implements IBaseEntity<Long> {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dic_code")
    private String dicCode;

    @Column(name = "label")
    private String label;

    @Column(name = "value")
    private String value;

    @Column(name = "remark")
    private String remark;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "create_time")
    private Date createTime;

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
