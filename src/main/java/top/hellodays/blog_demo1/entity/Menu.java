package top.hellodays.blog_demo1.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class Menu {

    private Long id;
    private Long pid;
    private String menu_perms;
    private String menu_url;
    @NotBlank(message = "权限名不能为空")
    private String menu_name;
    private String menu_type;
    private Integer status;
    private LocalDateTime create_time;
    private LocalDateTime update_time;


}
