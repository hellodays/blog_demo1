package top.hellodays.blog_demo1.shiro;


import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.util.AntPathMatcher;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import top.hellodays.blog_demo1.constant.ShiroConstant;
import top.hellodays.blog_demo1.enums.ResponseCode;
import top.hellodays.blog_demo1.model.vo.ResultVO;
import top.hellodays.blog_demo1.utils.JwtUtil;
import top.hellodays.blog_demo1.utils.RedisUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * JWT过滤器
 * 与Servlet中的Filter相似, 先于拦截器执行, 用于提前截获请求中的参数判断是否走Shiro处理
 */
@Slf4j
public class CustomJwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    private RedisUtil redis;

    private static String serverServletContextPath;
    private static String refreshTokenExpireTime;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();


    public CustomJwtFilter() {
        ResourceBundle resource = ResourceBundle.getBundle("application");
        refreshTokenExpireTime = resource.getString("config.refreshToken-expireTime");
    }

    /**
     * 登录认证
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        // 添加免登录接口
        if (secretFree(httpServletRequest)) {
            return true;
        }
        // 判断用户是否想要登入
        if (this.isLoginAttempt(request, response)) {
            try {
                // 进行Shiro的登录UserRealm
                this.executeLogin(request, response);
            } catch (Exception e) {
                // 认证出现异常，传递错误信息msg
                String msg = e.getMessage();
                // 获取应用异常(该Cause是导致抛出此throwable(异常)的throwable(异常))
                Throwable throwable = e.getCause();
                if (throwable instanceof SignatureVerificationException) {
                    // 该异常为JWT的AccessToken认证失败(Token或者密钥不正确)
                    msg = "token或者密钥不正确(" + throwable.getMessage() + ")";
                } else if (throwable instanceof TokenExpiredException) {
                    // 该异常为JWT的AccessToken已过期，判断RefreshToken未过期就进行AccessToken刷新
                    if (this.refreshToken(request, response)) {
                        return true;
                    } else {
                        msg = "token已过期(" + throwable.getMessage() + ")";
                    }
                } else {
                    // 应用异常不为空
                    if (throwable != null) {
                        // 获取应用异常msg
                        msg = throwable.getMessage();
                    }
                }
                /**
                 * 错误两种处理方式 1. 将非法请求转发到/401的Controller处理，抛出自定义无权访问异常被全局捕捉再返回Response信息 2.
                 * 无需转发，直接返回Response信息 一般使用第二种(更方便)
                 */
                // 直接返回Response信息
                this.response401(request, response, msg);
                return false;
            }
        }
        return true;
    }

    /**
     * 添加免密登录路径
     */
    private boolean secretFree(HttpServletRequest httpServletRequest) {
        String[] anonUrl = {"/register", "/login", "/swagger-ui.html", "/doc.html",
                "/webjars/**", "/swagger-resources", "/v2/api-docs", "/swagger-resources/**"};
        boolean match = false;
        String requestURI = httpServletRequest.getRequestURI();
        for (String u : anonUrl) {
            if (pathMatcher.match(serverServletContextPath + u, requestURI)) {
                match = true;
            }
        }
        return match;
    }

    /**
     * 这里我们详细说明下为什么重写 可以对比父类方法，只是将executeLogin方法调用去除了
     * 如果没有去除将会循环调用doGetAuthenticationInfo方法
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        this.sendChallenge(request, response);
        return false;
    }

    /**
     * 检测Header里面是否包含Authorization字段，有就进行Token登录认证授权
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader("Authorization");
        return authorization != null;
        //return true;
    }

    /**
     * 进行AccessToken登录认证授权
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader(ShiroConstant.REQUEST_AUTH_HEADER);
        CustomJwtToken token = new CustomJwtToken(authorization);
        // 提交给UserRealm进行认证，如果错误他会抛出异常并被捕获
        this.getSubject(request, response).login(token);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }

    /**
     * 刷新AccessToken，进行判断RefreshToken是否过期，未过期就返回新的AccessToken且继续正常访问
     */
    private boolean refreshToken(ServletRequest request, ServletResponse response) {
        // 拿到当前Header中Authorization的AccessToken(Shiro中getAuthzHeader方法已经实现)
        // String token = this.getAuthzHeader(request);
        HttpServletRequest req = (HttpServletRequest) request;
        String token = req.getHeader(ShiroConstant.REQUEST_AUTH_HEADER);
        // 获取当前Token的帐号信息
        String account = JwtUtil.getClaim(token, ShiroConstant.USERNAME);
        // 判断Redis中RefreshToken是否存在
        if (redis.hasKey(ShiroConstant.REFRESH_TOKEN + account)) {
            // Redis中RefreshToken还存在，获取RefreshToken的时间戳
            String currentTimeMillisRedis = redis.get(ShiroConstant.REFRESH_TOKEN + account).toString();
            // 获取当前AccessToken中的时间戳，与RefreshToken的时间戳对比，如果当前时间戳一致，进行AccessToken刷新
            if (Objects.equals(JwtUtil.getClaim(token, ShiroConstant.CURRENT_TIME_MILLIS), currentTimeMillisRedis)) {
                // 获取当前最新时间戳
                String currentTimeMillis = String.valueOf(System.currentTimeMillis());
                // 设置RefreshToken中的时间戳为当前最新时间戳，且刷新过期时间重新为30分钟过期(配置文件可配置refreshTokenExpireTime属性)
                redis.set(ShiroConstant.REFRESH_TOKEN + account, currentTimeMillis,
                        Integer.parseInt(refreshTokenExpireTime));
                // 刷新AccessToken，设置时间戳为当前最新时间戳
                token = JwtUtil.generateJwt(account, currentTimeMillis);
                // 将新刷新的AccessToken再次进行Shiro的登录
                CustomJwtToken customJwtToken = new CustomJwtToken(token);
                // 提交给UserRealm进行认证，如果错误他会抛出异常并被捕获，如果没有抛出异常则代表登入成功，返回true
                this.getSubject(request, response).login(customJwtToken);
                // 最后将刷新的AccessToken存放在Response的Header中的Authorization字段返回
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setHeader(ShiroConstant.REQUEST_AUTH_HEADER, token);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", ShiroConstant.REQUEST_AUTH_HEADER);
                return true;
            }
        }
        return false;
    }

    /**
     * 无需转发，直接返回Response信息
     */
    private void response401(ServletRequest req, ServletResponse resp, String msg) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()) {

            out.append(new ObjectMapper().writeValueAsString(ResultVO.failure(ResponseCode.UNAUTHORIZED, msg)));
        } catch (IOException e) {
            log.error("返回Response信息出现IOException异常:" + e.getMessage());
        }
    }

    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers",
                httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}