package top.hellodays.blog_demo1.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 选择性阉割前端传输对象
 * 登录, 只需要前端输入用户名及密码
 */
@Data
public class UserLoginDTO implements Serializable {

    @NotBlank(message = "昵称不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;

}