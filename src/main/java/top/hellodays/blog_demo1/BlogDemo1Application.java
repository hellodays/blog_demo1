package top.hellodays.blog_demo1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING) //解决Redis无法将配置类注入IOC问题(已存在Bean对象) 但是目前有更好的解决方案(看JedisConfig)
@SpringBootApplication
public class BlogDemo1Application {

    public static void main(String[] args) {
        SpringApplication.run(BlogDemo1Application.class, args);
    }

}
