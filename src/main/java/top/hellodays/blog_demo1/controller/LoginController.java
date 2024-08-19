package top.hellodays.blog_demo1.controller;


import cn.hutool.crypto.SecureUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import top.hellodays.blog_demo1.utils.UserConverterUtil;
import top.hellodays.blog_demo1.model.dto.UserLoginDTO;
import top.hellodays.blog_demo1.model.vo.ResultVO;
import top.hellodays.blog_demo1.service.UserService;
import top.hellodays.blog_demo1.utils.JwtUtil;
import top.hellodays.blog_demo1.constant.ShiroConstant;
import top.hellodays.blog_demo1.utils.RedisUtil;


/**
 * 登录控制器
 * 术业有专攻, 对每个请求进行细分, 不再写入UserController内造成数据冗余
 */
@Slf4j
@RestController
public class LoginController {


    @Value("${config.refreshToken-expireTime}")
    private String refreshTokenExpireTime;

    @Autowired
    private RedisUtil redis;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResultVO<?> login(@Validated UserLoginDTO userLoginDTO, HttpServletResponse response) throws Exception {

        log.info("登录Controller层开始认证...");
        String username = userLoginDTO.getUsername();
        String password = SecureUtil.md5(userLoginDTO.getPassword());
        String realPassword = userService.getUserByUserName(username).getPassword();
        Integer userstatus = userService.getUserByUserName(username).getStatus(); //账户锁定判断?
        if (realPassword == null) {
            throw new UnknownAccountException();
        } else if (!realPassword.equals(password)) {
            throw new IncorrectCredentialsException();
        } else if (userstatus == 1) {
            throw new AccountException("用户被锁定，无法登录");
        }
        log.info("结束认证...");

        log.info("数据库对象认证通过, 接下来进行Redis缓存及信息返回...");

        // 清除可能存在的shiro权限信息缓存
        if (redis.hasKey(ShiroConstant.PREFIX_SHIRO_CACHE + username)) {
            redis.del(ShiroConstant.PREFIX_SHIRO_CACHE + username);
        }
        // 设置RefreshToken，时间戳为当前时间戳，直接设置即可(不用先删后设，会覆盖已有的RefreshToken)
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        redis.set(ShiroConstant.REFRESH_TOKEN + username, currentTimeMillis,
                Integer.parseInt(refreshTokenExpireTime));
        // 从Header中Authorization返回AccessToken，时间戳为当前时间戳
        String token = JwtUtil.generateJwt(username, currentTimeMillis);
        response.setHeader(ShiroConstant.REQUEST_AUTH_HEADER, token);
        response.setHeader("Access-Control-Expose-Headers", ShiroConstant.REQUEST_AUTH_HEADER);

        log.info("当前登录时间戳为: " + currentTimeMillis);
        log.info("登录生成的Token为: " + token);

        //获取当前的用户
//        Subject subject = SecurityUtils.getSubject();
//        if (!subject.isAuthenticated()){
//            CustomJwtToken jwtToken = new CustomJwtToken(token);
//            // 执行登陆方法
//            subject.login(jwtToken);
//        }

        return ResultVO.success(UserConverterUtil.convertUser(userService.getUserByUserName(userLoginDTO.getUsername())));

    }

    @RequiresAuthentication
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResultVO<?> logout(HttpServletRequest request) {


//        String token = request.getHeader(ShiroConstant.REQUEST_AUTH_HEADER);
//        CustomJwtToken jwtToken = new CustomJwtToken(token);
//        Subject current = SecurityUtils.getSubject();
//        current.login(jwtToken);
//        current.logout();
        return ResultVO.success();
    }

}