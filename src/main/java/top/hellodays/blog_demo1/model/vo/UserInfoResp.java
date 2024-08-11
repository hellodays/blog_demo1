package top.hellodays.blog_demo1.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 选择性阉割返回前端对象
 * 登录成功, 返回部分用户信息供前端进行展示
 */
@Data
public class UserInfoResp implements Serializable {

    private Integer id;
    private String username;
    private String email;
    private String avatar;

}