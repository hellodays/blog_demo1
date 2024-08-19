package top.hellodays.blog_demo1.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hellodays.blog_demo1.mapper.UserMapper;
import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.service.UserService;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;


    @Override
    public User getUserByUserName(String username) {

        User userdb = userMapper.findUserByName(username);
        if (userdb != null) {
            return userdb;
        }
        return null;
    }

    @Override
    public void createUser(User user) {

        User userdb = userMapper.findUserByName(user.getUsername());
        if (userdb == null) {
            userMapper.addUser(user);
        }
    }

    @Override
    public User getAdminUser(User user) {
        return null;
    }

    @Override
    public User getNormalUser(User user) {

        User userdb = userMapper.login(user.getUsername(), user.getPassword());
        if (userdb != null) {
            return userdb;
        } else {
            return null;
        }
    }

    @Override
    public void deleteUser(Integer id) {
        userMapper.deleteUser(id);
    }

    @Override
    public void updateUser(User user) {
        userMapper.updateUser(user);
    }
}
