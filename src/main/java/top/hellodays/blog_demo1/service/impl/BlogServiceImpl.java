package top.hellodays.blog_demo1.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.hellodays.blog_demo1.mapper.BlogMapper;
import top.hellodays.blog_demo1.service.BlogService;
import top.hellodays.blog_demo1.entity.Blog;

import java.util.List;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    BlogMapper blogMapper;

    @Override
    public PageInfo<Blog> getBlogs(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Blog> blogList = blogMapper.findAllBlog();
        PageInfo<Blog> pageInfo = new PageInfo<>(blogList);
        return pageInfo;
    }

    @Override
    public Blog getBlogById(Long id) {
        Blog blogdb = blogMapper.findBlogById(id);
        if (blogdb != null) {
            return blogdb;
        } else {
            return null;
        }
    }

    @Override
    public void addBlog(Blog blog) {
        if (blog != null) {
            blogMapper.addBlog(blog);
        }
    }

    @Override
    public void editBlog(Blog blog) {
        if (blog != null) {
            blogMapper.updateBlog(blog);
        }
    }

    @Override
    public int deleteBlog(Integer id) {
        return blogMapper.deleteBlog(id);
    }

}
