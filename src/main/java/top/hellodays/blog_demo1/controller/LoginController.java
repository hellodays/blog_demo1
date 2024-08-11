package top.hellodays.blog_demo1.controller;


import cn.hutool.core.lang.Assert;
import cn.hutool.crypto.SecureUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.enums.ResponseCode;
import top.hellodays.blog_demo1.model.dto.UserLoginDTO;
import top.hellodays.blog_demo1.model.vo.Response;
import top.hellodays.blog_demo1.model.vo.UserInfoResp;
import top.hellodays.blog_demo1.service.UserService;
import top.hellodays.blog_demo1.utils.JWTUtil;
import top.hellodays.blog_demo1.utils.UserConverter;


/**
 * 登录控制器
 * 术业有专攻, 对每个请求进行细分, 不再写入UserController内造成数据冗余
 */
@RestController
public class LoginController {

    @Autowired
    UserService userService;


    //用户登录接口
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public Response<UserInfoResp> login(@Validated UserLoginDTO userLoginDTO, HttpServletResponse httpServletResponse) { //@Validated注解用来配合实体参数完整性的校验

        //判断是否存在
        User userdb = userService.getUserByUserName(userLoginDTO.getUsername());
        Assert.notNull(userdb); //断言重现！但是Java, 很明显抛出错误(异常)之后应该进行捕获处理, 这里交给了全局异常捕获, 详见exception包
        //判断密码正确
        //存进数据库的密码理应使用md5工具进行加密, 因而拿出来的数据也是md5, 所以需要使用Hutu工具包来对传进来的密码再次加密后进行对比(md5加密同字符串好像结果一样)
        if (!(userdb.getPassword().equals(SecureUtil.md5(userLoginDTO.getPassword())))) {
            return Response.failure(ResponseCode.UNAUTHORIZED, "密码错误");
        } else {
            //一旦登陆成功就应该将Token颁布给用户(该项目核心)
            String token = JWTUtil.createToken(userdb);
            //最后一步将所需的核心数据返回前端(这一步用到了Servlet的内容, 记得及时回顾)
            httpServletResponse.setHeader("Authorization", token); //这里是把token放进了Authorization头中
            httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");

            return Response.success(UserConverter.convertUser(userdb));
        }
    }

}