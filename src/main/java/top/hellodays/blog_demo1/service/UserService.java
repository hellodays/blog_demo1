package top.hellodays.blog_demo1.service;

import org.springframework.stereotype.Service;
import top.hellodays.blog_demo1.entity.User;

@Service
public interface UserService {

    //注册部分
    User getUserByUserName(String username); //判断有无再作注册
    void createUser(User user); //注册

    //登录部分作级别区分, 为后续管理员普通用户登录功能作准备
    User getAdminUser(User user); //获取管理员级别用户
    User getNormalUser(User user); //获取普通用户

    //用户部分的CRUD, 为后续管理员界面的增删改查作准备
    void deleteUser(Integer id); //后台页面删除用户
    void updateUser(User user); //后台或前台更新用户信息


}