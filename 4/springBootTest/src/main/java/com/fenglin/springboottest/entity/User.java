package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("star_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 账号（唯一） */
    private String username;

    /** 密码（明文存储，演示项目不做加密） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 权限：0 管理员 / 1 默认（普通修士），注册用户默认 1 */
    private Integer permission;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
