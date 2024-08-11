package top.hellodays.blog_demo1.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import top.hellodays.blog_demo1.entity.User;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * JWT工具类编码优化
 * 相较于之前, 作出如下改进
 * 将生成Token的所需参数进行变动
 */
public class JWTUtil {

    //属性部分结合参考的文章取最关键的几项为例
    private final static String ENCRYPT_KEY = "f4e2e52034348f86b67cde581c0f9eb5"; // 加密的密钥

    private final static int EXPIRE_TIME = 10080; // token 过期时间，单位分钟

    //非必要项(签发人)
    private static final String ISSUER = "_days";

    //Token核心方法
    //创建Token
    public static String createToken(User user) {

        //创建关于Token过期时间的实例(单位分钟)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, EXPIRE_TIME); //注意还要调用相应方法才能获取时间

        //创建jwt builder
        JWTCreator.Builder builder = JWT.create();

        //payload(有效载荷部分)
        Map<String, Integer> payload = new HashMap<>();
        payload.put("id", user.getId());
        //这里削减了内容, 循环留着用来学习表达式语法
        payload.forEach((k, v) -> {
            builder.withClaim(k, v);
        });

        //最后配置token
        String token = builder
                .withIssuer(ISSUER) // 设置发布者
                .withExpiresAt(calendar.getTime()) // 设置过期时间
                .sign(Algorithm.HMAC256(ENCRYPT_KEY)); // 加密

        return token;
    }

    //校验Token
    //这里的写法实际不完整, 仅作演示, 完整运用参考顶部链接
    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(ENCRYPT_KEY)).build().verify(token);
    }

}
