package com.tbs.app.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;
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
public class SystemUser implements Serializable {
    private static final long serialVersionUID = 1L;
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

    public SystemUser setId(Long id) {
        this.id = id;
        return this;
    }

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

    public SystemUser setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public SystemUser setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public SystemUser setFlag(Integer flag) {
        this.flag = flag;
        return this;
    }
}
