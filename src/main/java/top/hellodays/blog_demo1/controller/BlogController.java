package top.hellodays.blog_demo1.controller;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hellodays.blog_demo1.entity.Blog;
import top.hellodays.blog_demo1.enums.ResponseCode;
import top.hellodays.blog_demo1.model.vo.ResultVO;
import top.hellodays.blog_demo1.service.BlogService;
import top.hellodays.blog_demo1.utils.ShiroProfileUtil;

import java.time.LocalDateTime;
import java.util.Objects;


/**
 * 博客Controller
 * 由于使用了Shiro的权限注解, 导致整个Controller失效(与AOP的Starter冲突)
 * 解决方案: https://juejin.cn/post/6844903593510699016
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    HttpServletRequest request;
    @Autowired
    BlogService blogService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResultVO getAll(Integer pageNum, Integer pageSize) {

        PageInfo<Blog> pageInfo = blogService.getBlogs(pageNum, pageSize);

        return ResultVO.success(pageInfo);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResultVO getById(@PathVariable Long id) {
        Blog blogdb = blogService.getBlogById(id);
        if (blogdb != null) {
            return ResultVO.success(blogdb);
        } else {
            return ResultVO.failure(ResponseCode.PARAMS_IS_INVALID, "查找不到该博客信息");
        }
    }

    @RequiresAuthentication
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResultVO add(@Validated @RequestBody Blog blog, HttpServletRequest request) {

        System.out.println("当前登录的用户: " + SecurityUtils.getSubject().getPrincipal().toString());

        //先将当前登陆用户的相关信息以及相关时间数据封装
        blog.setUser_id(ShiroProfileUtil.getUserInfo().getId());
        blog.setCreated(LocalDateTime.now()); //更新日期, 这里作为编辑日期(关于日期的数据类型有坑)
        blog.setStatus(0);

        blogService.addBlog(blog);
        return ResultVO.success();
    }


    @RequiresAuthentication
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public ResultVO edit(@Validated @RequestBody Blog blog) {

        Blog blogdb;
        //有效判断, 被修改的Blog是否存在ID
        if (blog.getId() != null) {
            blogdb = blogService.getBlogById(blog.getId());
            //存在ID, 拿出数据, 那么该博客的所属用户ID是否与当前登录的用户ID一致, 这里用断言+Shiro获取当前
            //关于Shiro的认证流程, 再次补充: https://www.cnblogs.com/beyond-tester/p/17627555.html
            Assert.isTrue(Objects.equals(blogdb.getUser_id(), ShiroProfileUtil.getUserInfo().getId()), "非博客发布者, 无编辑权限");
        } else {
            //确认有操作权限, 将前端传过来博客数据进行加工存储
            blogdb = new Blog();
            blogdb.setUser_id(ShiroProfileUtil.getUserInfo().getId());
            blogdb.setCreated(LocalDateTime.now()); //更新日期, 这里作为编辑日期(关于日期的数据类型有坑)
            blogdb.setStatus(0);
        }
        //最后调用工具类将传输过来的博客数据进行迁移, 其中忽略掉上边已经手动设置的四个字段变量
        BeanUtil.copyProperties(blog, blogdb, "id", "userId", "created", "status");
        blogService.editBlog(blogdb);

        return ResultVO.success();
    }

    @RequiresAuthentication
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public ResultVO delete(@PathVariable Integer id) {

        //这里套用了第一版mall_demo项目的判断方式, 忘记就回顾
        int result = blogService.deleteBlog(id);
        if (result >= 1) {
            return ResultVO.success();
        } else {
            return ResultVO.failure(ResponseCode.FAIL, "删除失败");
        }

    }

}