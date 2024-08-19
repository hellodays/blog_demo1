package top.hellodays.blog_demo1.shiro;

import org.apache.shiro.authc.AuthenticationToken;


/**
 * 自定义Shiro的Token
 * 默认Shiro的Token采用USERNAME+PASSWORD的方式进行创建, 但由于使用了JWT, 加密方式与默认Token不匹配
 * 故重写
 */
public class CustomJwtToken implements AuthenticationToken {

    private final String token;

    public CustomJwtToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}