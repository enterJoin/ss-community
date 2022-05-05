package com.atlxw.community.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 自增ID
     */
    private Integer id;
    /**
     * 用户名
     */
    private String username;
    /**
     * SHA256加密过的密码，由java负责加密
     */
    private String encrypted_password;
    /**
     * 电子邮件
     */
    private String email;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 座右铭(个性签名) 可为空
     */
    private String motto;
    /**
     * 用户头像图片的物理路径 可为空
     */
    private String head_photo_path;
    /**
     * 用户头像图片的URl 可为空
     */
    private String head_photo_url;
    /**
     * 用户头像图片的生成名称 可为空
     */
    private String head_photo_name;
    /**
     * 注册时间
     */
    private Timestamp register_time;
    /**
     * 最后登录的时间
     */
    private Timestamp last_login_time;
    /**
     * 上一次修改用户资料时间
     */
    private Timestamp last_update_info_time;
    /**
     * 上一次登录时的客户端ip
     */
    private String last_login_ip;
}
