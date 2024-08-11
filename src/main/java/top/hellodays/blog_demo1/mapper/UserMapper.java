package top.hellodays.blog_demo1.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.hellodays.blog_demo1.entity.User;

@Mapper
public interface UserMapper {

    int addUser(User user);

    int deleteUser(Integer id);

    int updateUser(User user);

    User findUserById(Integer id);

    User findUserByName(String keyword);

    User login(String un, String pw);

}