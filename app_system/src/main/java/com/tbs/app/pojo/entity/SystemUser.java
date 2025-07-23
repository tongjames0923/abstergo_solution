package com.tbs.app.pojo.entity;

import com.tbs.app.pojo.IBaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * 系统用户
 *
 * @author abstergo
 */
@Table(name = "sys_user", indexes = {@Index(name = "idx_username_password", columnList = "username,password"),
    @Index(name = "idx_nickname", columnList = "nickname"), @Index(name = "idx_flag", columnList = "flag"),
    @Index(name = "idx_time", columnList = "create_time,update_time")})
@Entity
@Getter
public class SystemUser implements IBaseEntity<Long> {
    private static final long serialVersionUID = 1L;
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户名
     */
    @Column(name = "username", length = 72, unique = true, nullable = false)
    private String username;
    /**
     * 密码
     */
    @Column(name = "password", length = 72, nullable = false)
    private String password;
    /**
     * 昵称
     */
    @Column(name = "nickname", length = 32, nullable = false)
    private String nickname;
    /**
     * 头像
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * 备注
     */
    @Column(name = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "create_time")
    @Setter
    private Date createTime;
    /**
     * 修改时间
     */
    @Column(name = "update_time")
    @Setter
    private Date updateTime;
    /**
     * 状态
     */
    @Column(name = "flag")
    @Setter
    private Integer flag;

    public SystemUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public SystemUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public SystemUser setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public SystemUser setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public SystemUser setRemark(String remark) {
        this.remark = remark;
        return this;
    }

}
