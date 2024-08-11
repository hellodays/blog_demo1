package top.hellodays.blog_demo1.shiro;

import org.apache.shiro.authc.AuthenticationToken;


/**
 * 取代Shiro取生token
 * 由于使用了JWT进行Token的生成与验证, 所以这里不再需要Shiro原声的Token
 */
public class JwtToken implements AuthenticationToken {

    private String token;

    public JwtToken(String token) {
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