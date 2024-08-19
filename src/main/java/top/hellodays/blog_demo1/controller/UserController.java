package top.hellodays.blog_demo1.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.hellodays.blog_demo1.constant.ShiroConstant;
import top.hellodays.blog_demo1.model.vo.ResultVO;
import top.hellodays.blog_demo1.shiro.CustomJwtToken;
import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.enums.ResponseCode;
import top.hellodays.blog_demo1.service.UserService;
import top.hellodays.blog_demo1.utils.JwtUtil;


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


    @RequiresAuthentication
    @RequestMapping(value = "/testinsert", method = RequestMethod.POST)
    public void test(@Validated @RequestBody User user) {
        userService.createUser(user);
    }


    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ResultVO current(HttpServletRequest request) {

        String ttooken = (String) SecurityUtils.getSubject().getPrincipal();
        System.out.println("测试无手动登录状态下token " + ttooken);

        String token = request.getHeader(ShiroConstant.REQUEST_AUTH_HEADER);
        System.out.println(token);
        CustomJwtToken customJwtToken = new CustomJwtToken(token);

        try {
            User user = new User();
            Subject subject = SecurityUtils.getSubject();
            subject.login(customJwtToken);
            if (subject != null) {
                token = (String) subject.getPrincipal();

                if (StringUtils.isNotBlank(token)) {
                    String account = JwtUtil.getClaim(token, ShiroConstant.USERNAME);
                    if (StringUtils.isNotBlank(account)) {
                        user = userService.getUserByUserName(account);
                    }
                }
            }
            return ResultVO.success(user);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultVO.failure(ResponseCode.FAIL, e.getMessage());
        }
    }

}
