package com.tbs.app.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.app.pojo.IBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * @author tongj
 */
@Entity
@Table(name = "sys_role", uniqueConstraints = @UniqueConstraint(columnNames = {"role"}),
    indexes = {@Index(name = "idx_flag", columnList = "flag"),
        @Index(name = "idx_time", columnList = "create_time,update_time")})
@Getter
@Setter
public class SystemRole implements IBaseEntity<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    /**
     * 角色
     */
    @Column(name = "role", length = 128, unique = true, nullable = false)
    private String role;
    /**
     * 描述
     */
    @Column(name = "description")
    private String description;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    private Date updateTime;
    /**
     * 状态
     */
    @Column(name = "flag")
    private Integer flag;
}
