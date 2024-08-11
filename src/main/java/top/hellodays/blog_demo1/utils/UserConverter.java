package top.hellodays.blog_demo1.utils;

import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.model.vo.UserInfoResp;

/**
 * 用户消息体与实体转换工具类
 * 初识Springboot Crud项目内容回归, 喜闻乐见, 相信阿里架构师的实力
 */

public class UserConverter {

    public static UserInfoResp convertUser(User user) {

        UserInfoResp userInfoResp = new UserInfoResp();
        userInfoResp.setId(user.getId());
        userInfoResp.setUsername(user.getUsername());
        userInfoResp.setEmail(user.getEmail());
        userInfoResp.setAvatar(user.getAvatar());

        return userInfoResp;
    }

}