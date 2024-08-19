package top.hellodays.blog_demo1.utils;


import cn.hutool.core.codec.Base64;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import top.hellodays.blog_demo1.constant.ShiroConstant;

import java.util.Date;

/**
 * JWT工具类
 * 用于生成校验以及获取JWT
 * 该工具类已基本重写, 对比之前的项目参考价值不大
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * 静态资源, 通过application.properties读取
     */
    @Value("${config.encrypt-jwtKey}")
    private String ENCRYPT_JWT_KEY;
    @Value("${config.accessToken-expireTime}")
    private String ACCESS_TOKEN_EXPIRE_TIME;
    private static String ENCRYPT_JWT_KEY_STATIC;
    private static String ACCESS_TOKEN_EXPIRE_TIME_STATIC;

    //与上方配套, 不创建该方法会无法注入属性
    @PostConstruct
    private void init() {
        ENCRYPT_JWT_KEY_STATIC = ENCRYPT_JWT_KEY;
        ACCESS_TOKEN_EXPIRE_TIME_STATIC = ACCESS_TOKEN_EXPIRE_TIME;
    }

    /**
     * 效验token是否正确
     */
    public static boolean verify(String token) {
        try {
            String secret = Base64.decodeStr(ENCRYPT_JWT_KEY_STATIC);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            jwtVerifier.verify(token);
            return true;
        } catch (Exception e) {
            log.error("token认证失败异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取Jwt payload的内容
     */
    public static String getClaim(String token, String claim) {
        try {
            // 只能输出String类型，如果是其他类型则返回null
            return JWT.decode(token).getClaim(claim).asString();
        } catch (JWTDecodeException e) {
            log.error("解密token中的公共信息异常：{}" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成Jwt
     */
    public static String generateJwt(String username, String currentTimeMillis) {
        try {
            // 获取jwt过期时间（单位为毫秒）
            Date expireDate = new Date(System.currentTimeMillis() + Long.parseLong(ACCESS_TOKEN_EXPIRE_TIME_STATIC) * 1000);
            // 获取签名
            String secret = Base64.decodeStr(ENCRYPT_JWT_KEY_STATIC);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            // 生成Jwt
            return JWT.create()
                    // 存放username
                    .withClaim(ShiroConstant.USERNAME, username)
                    // 存放当前时间戳
                    .withClaim(ShiroConstant.CURRENT_TIME_MILLIS, currentTimeMillis)
                    .withExpiresAt(expireDate)
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("token生成失败异常：{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


}

