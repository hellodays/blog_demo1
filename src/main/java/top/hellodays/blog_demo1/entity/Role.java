package top.hellodays.blog_demo1.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class Role {

    private Long id;
    @NotBlank(message = "角色名不能为空")
    private String role_name;
    private String role_desc;
    private Integer status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;


}
