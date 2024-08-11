package top.hellodays.blog_demo1.controller;

import com.github.pagehelper.PageInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.hellodays.blog_demo1.entity.Blog;
import top.hellodays.blog_demo1.model.vo.Response;
import top.hellodays.blog_demo1.service.BlogService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BlogControllerTest {

    @Autowired
    BlogService blogService;

    @Test
    void getAll() {
        PageInfo<Blog> pageInfo = blogService.getBlogs(0, 1);
        System.out.println(pageInfo);
    }
}