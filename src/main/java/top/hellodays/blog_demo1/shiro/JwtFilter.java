package top.hellodays.blog_demo1.shiro;

import cn.hutool.json.JSONUtil;
import com.alibaba.druid.util.StringUtils;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.stereotype.Component;
import top.hellodays.blog_demo1.utils.JWTUtil;
import top.hellodays.blog_demo1.model.vo.Response;
import top.hellodays.blog_demo1.enums.ResponseCode;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 关于@Slf4j注解引入原因: https://blog.csdn.net/leijie0322/article/details/133138074
 * 以及该注解是干嘛的: https://juejin.cn/post/6921246140822192141
 */
@Slf4j
@Component
public class JwtFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        // 获取 token
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if (StringUtils.isEmpty(jwt)) {
            return null;
        }
        return new JwtToken(jwt);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            return true;
        } else {

            //先验证Token信息, 假设没有问题则直接放行

            try {
                JWTUtil.verify(token);
                return true;
            } catch (SignatureVerificationException e) {
                log.error("无效签名", e);
            } catch (TokenExpiredException e) {
                log.error("token过期", e);
            } catch (AlgorithmMismatchException e) {
                log.error("算法不一致", e);
            } catch (Exception e) {
                log.error("token 为空或无效！", e);
            }

            // 执行自动登录
            return executeLogin(servletRequest, servletResponse);
        }
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        try {
            //处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            Response r = Response.failure(ResponseCode.UNAUTHORIZED, throwable.getMessage());

            //对给前端看的Part, 使用了Servlet内容
            String json = JSONUtil.toJsonStr(r);
            httpServletResponse.getWriter().print(json);

        } catch (IOException ioException) {
            log.error("IOException", e);
        }
        return false;
    }

}
