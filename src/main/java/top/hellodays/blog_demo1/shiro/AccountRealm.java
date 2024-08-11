package top.hellodays.blog_demo1.shiro;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.model.vo.UserInfoResp;
import top.hellodays.blog_demo1.service.UserService;
import top.hellodays.blog_demo1.utils.JWTUtil;

@Slf4j
@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * AuthorizationInfo: 角色的权限信息, 该方法一般用作作授权
     * 只有当需要检测用户权限的时候才会调用此方法，例如 checkRole,checkPermission 之类的
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    /**
     * AuthenticationInfo: 用户的角色信息, 该方法一般用作验证
     * 默认使用此方法进行用户名正确与否验证，错误抛出异常即可。
     * 认证信息 (身份验证)
     * Authentication 是用来验证用户身份
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getCredentials();
        if (token == null) {
            throw new NullPointerException("jwtToken 不允许为空");
        }
        log.info("jwt----------------->{}", token);
        String username;
        try {
            // 根据token获得登录用户的userName
            username = JWTUtil.verify(token).getClaim("username").asString();
        } catch (Exception e) {
            throw new AuthenticationException("该token非法，可能被篡改或过期");
        }
        User user = userService.getUserByUserName(username);
        if (user == null) {
            throw new UnknownAccountException("账户不存在！");
        }
        if (user.getStatus() == -1) {
            throw new LockedAccountException("账户已被锁定！");
        }
        UserInfoResp userInfoResp = new UserInfoResp();
        BeanUtil.copyProperties(user, userInfoResp); //将前拷到后
        log.info("profile----------------->{}", userInfoResp.toString());
        return new SimpleAuthenticationInfo(userInfoResp, user.getPassword(), getName()); //这玩意会自动将数据库里拿到的对象与Token信息进行校验
    }
}