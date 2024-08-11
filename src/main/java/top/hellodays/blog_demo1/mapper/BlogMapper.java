package top.hellodays.blog_demo1.mapper;

import org.apache.ibatis.annotations.Mapper;
import top.hellodays.blog_demo1.entity.Blog;

import java.util.List;

@Mapper
public interface BlogMapper {

    int addBlog(Blog blog);

    int deleteBlog(Integer id);

    int updateBlog(Blog blog);

    Blog findBlogById(Integer id);

    Blog findBlogByName(String keyword);

    List<Blog> findAllBlog();

}