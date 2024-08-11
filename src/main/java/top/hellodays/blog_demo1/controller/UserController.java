package top.hellodays.blog_demo1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.service.UserService;


/**
 * 在控制层中, 新增了实体校验的内容
 * 主要是通过注解的方式, 如@Validated
 * 参考: https://juejin.cn/post/7114109877915500558
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;


    @RequestMapping(value = "/testinsert", method = RequestMethod.POST)
    public void test(@Validated User user) {
        userService.createUser(user);
    }

}
