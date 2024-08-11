package top.hellodays.blog_demo1.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Blog {

    private Integer id;
    private Integer user_id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String description;
    @NotBlank(message = "内容不能为空")
    private String content;
    private LocalDateTime created;
    private Integer status;

}
