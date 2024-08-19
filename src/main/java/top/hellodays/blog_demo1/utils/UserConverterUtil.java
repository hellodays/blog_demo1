package top.hellodays.blog_demo1.utils;

import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.model.vo.UserProfileVO;

/**
 * 用户消息体与实体转换工具类
 * 初识Springboot Crud项目内容回归, 喜闻乐见, 相信阿里架构师的实力
 */
public class UserConverterUtil {

    public static UserProfileVO convertUser(User user) {

        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setId(user.getId());
        userProfileVO.setUsername(user.getUsername());
        userProfileVO.setEmail(user.getEmail());
        userProfileVO.setAvatar(user.getAvatar());

        return userProfileVO;
    }

}