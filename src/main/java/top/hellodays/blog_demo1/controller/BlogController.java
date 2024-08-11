package top.hellodays.blog_demo1.controller;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageInfo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hellodays.blog_demo1.entity.Blog;
import top.hellodays.blog_demo1.enums.ResponseCode;
import top.hellodays.blog_demo1.model.vo.Response;
import top.hellodays.blog_demo1.model.vo.UserInfoResp;
import top.hellodays.blog_demo1.service.BlogService;

import java.time.LocalDateTime;


/**
 * 博客Controller
 * 由于使用了Shiro的权限注解, 导致整个Controller失效(与AOP的Starter冲突)
 * 解决方案: https://juejin.cn/post/6844903593510699016
 *
 * */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    BlogService blogService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Response getAll(Integer pageNum, Integer pageSize) {

        PageInfo<Blog> pageInfo = blogService.getBlogs(pageNum, pageSize);

        return Response.success(pageInfo);

    }

    @RequestMapping("/{id}")
    public Response getById(@PathVariable Integer id) {
        Blog blogdb = blogService.getBlogById(id);
        if (blogdb != null) {
            return Response.success(blogdb);
        } else {
            return Response.failure(ResponseCode.PARAMS_IS_INVALID, "查找不到该博客信息");
        }
    }

    @RequiresAuthentication
    @RequestMapping("/edit")
    public Response edit(@Validated @RequestBody Blog blog) {

        Blog blogdb = null;
        //有效判断, 被修改的Blog是否存在ID
        if (blog.getId() != null) {
            blogdb = blogService.getBlogById(blog.getId());
            //存在ID, 拿出数据, 那么该博客的所属用户ID是否与当前登录的用户ID一致, 这里用断言+Shiro获取当前
            //关于Shiro的认证流程, 再次补充: https://www.cnblogs.com/beyond-tester/p/17627555.html
            Assert.isTrue(blogdb.getUser_id().equals(((UserInfoResp) (SecurityUtils.getSubject().getPrincipal())).getId()), "非博客发布者, 无编辑权限");
        } else {
            //确认有操作权限, 将前端传过来博客数据进行加工存储
            blogdb = new Blog();
            blogdb.setUser_id(((UserInfoResp) (SecurityUtils.getSubject().getPrincipal())).getId());
            blogdb.setCreated(LocalDateTime.now()); //更新日期, 这里作为编辑日期(关于日期的数据类型有坑)
            blogdb.setStatus(0);
        }
        //最后调用工具类将传输过来的博客数据进行迁移, 其中忽略掉上边已经手动设置的四个字段变量
        BeanUtil.copyProperties(blog, blogdb, "id", "userId", "created", "status");
        blogService.editBlog(blogdb);

        return Response.success();
    }

    @RequiresAuthentication
    @RequestMapping("/delete/{id}")
    public Response delete(@PathVariable Integer id) {

        //这里套用了第一版mall_demo项目的判断方式, 忘记就回顾
        int result = blogService.deleteBlog(id);
        if (result >= 1) {
            return Response.success();
        } else {
            return Response.failure(ResponseCode.FAIL, "删除失败");
        }

    }


}
