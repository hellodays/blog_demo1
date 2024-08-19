package top.hellodays.blog_demo1.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.hellodays.blog_demo1.constant.ShiroConstant;
import top.hellodays.blog_demo1.entity.User;
import top.hellodays.blog_demo1.model.vo.UserProfileVO;
import top.hellodays.blog_demo1.service.UserService;
import top.hellodays.blog_demo1.utils.JwtUtil;
import top.hellodays.blog_demo1.utils.RedisUtil;
import top.hellodays.blog_demo1.utils.UserConverterUtil;

import java.util.Objects;

/**
 * 自定义Shiro的Realm模块
 * 基本就是一个授权验权中心, Shiro的校验授权模块
 */
@Slf4j
@Component
public class CustomJwtRealm extends AuthorizingRealm {

    @Autowired
    private RedisUtil redis;

    @Autowired
    private UserService userService;

//    @Autowired
//    private UmsAdminService umsAdminService;
//
//    @Autowired
//    private UmsRoleService umsRoleService;
//
//    @Autowired
//    private UmsPermissionService umsPermissionService;


    /**
     * 大坑！，必须重写此方法，不然Shiro会报错
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof CustomJwtToken;
    }


    /**
     * 授权认证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
//        // 从token中获取username
//        String username = JwtUtil.getClaim(principalCollection.toString(), ShiroConstant.USERNAME);
//        // 根据用户名称获取角色名称集合
//        List<UmsRoleDTO> umsRoleDTOList = umsRoleService.findRolesByUsername(username);
//        Set<String> roleNameSet = umsRoleDTOList.stream().map(UmsRoleDTO::getName).collect(Collectors.toSet());
//        // 根据角色id集合获取权限值集合
//        List<Long> userIdList = umsRoleDTOList.stream().map(UmsRoleDTO::getId).collect(Collectors.toList());
//        List<UmsPermissionDTO> permissionList = umsPermissionService.findPermissionsByRoleIds(userIdList);
//        Set<String> permissionValueSet = permissionList.stream().map(UmsPermissionDTO::getValue).collect(Collectors.toSet());
//        // 将角色名称集合和权限值集合放入到shiro认证信息中
//        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
//        simpleAuthorizationInfo.setRoles(roleNameSet);
//        simpleAuthorizationInfo.setStringPermissions(permissionValueSet);
//        return simpleAuthorizationInfo;
        return null;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        // 获取token信息
        String token = (String) authenticationToken.getCredentials();
        if (StringUtils.isBlank(token)) {
            throw new AuthenticationException("TOKEN_CANNOT_BE_EMPTY");
        }
        // 使用jwtUtil解密获取Username
        String username = JwtUtil.getClaim(token, ShiroConstant.USERNAME);
        if (StringUtils.isBlank(username)) {
            throw new AuthenticationException("TOKEN_INVALID");
        }
        User user = userService.getUserByUserName(username);
        if (Objects.isNull(user)) {
            throw new AuthenticationException("USER_DIDNT_EXISTED");
        }
        // 开始认证，要AccessToken认证通过，且Redis中存在RefreshToken，且两个Token时间戳一致
        if (JwtUtil.verify(token) && redis.hasKey(ShiroConstant.REFRESH_TOKEN + username)) {
            // 获取RefreshToken的时间戳
            String currentTimeMillisRedis = redis.get(ShiroConstant.REFRESH_TOKEN + username).toString();
            // 获取AccessToken时间戳，与RefreshToken的时间戳对比
            if (Objects.equals(JwtUtil.getClaim(token, ShiroConstant.CURRENT_TIME_MILLIS), currentTimeMillisRedis)) {
                UserProfileVO profile = UserConverterUtil.convertUser(user);
                return new SimpleAuthenticationInfo(profile, token, getName());
            }
        }
        throw new AuthenticationException("TOKEN_EXPIRED_OR_INCORRECT");
    }
}