package top.hellodays.blog_demo1.service;

import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import top.hellodays.blog_demo1.entity.Blog;

@Service
public interface BlogService {

    //获取博客的两种方式
    PageInfo<Blog> getBlogs(Integer pageNum, Integer pageSize);
    Blog getBlogById(Integer id);

    //修改博客
    void editBlog(Blog blog);

    //删除博客
    int deleteBlog(Integer id);

}