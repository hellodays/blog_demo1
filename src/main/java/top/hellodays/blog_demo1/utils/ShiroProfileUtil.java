package top.hellodays.blog_demo1.utils;

import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Component;
import top.hellodays.blog_demo1.model.vo.UserProfileVO;


/**
 * 该工具类用于获取在Shiro框架下登录的用户信息并将其转成VO形式方便调取
 */
@Component
public class ShiroProfileUtil {

    //返回类型为用户资料VO
    public static UserProfileVO getUserInfo() {

        return (UserProfileVO) SecurityUtils.getSubject().getPrincipal();
    }

}