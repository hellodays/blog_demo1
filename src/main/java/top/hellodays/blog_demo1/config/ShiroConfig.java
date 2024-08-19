package top.hellodays.blog_demo1.config;

import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import top.hellodays.blog_demo1.shiro.CustomCacheManager;
import top.hellodays.blog_demo1.shiro.CustomJwtFilter;
import top.hellodays.blog_demo1.shiro.CustomJwtRealm;

import java.util.HashMap;
import java.util.Map;

/**
 * Shiro配置类
 */
@Slf4j
@Configuration
public class ShiroConfig {


    /**
     * 配置使用自定义Realm
     */
    @Bean("securityManager")
    public DefaultWebSecurityManager securityManager(CustomJwtRealm customJwtRealm, RedisTemplate<String, Object> template) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 使用自定义Realm
        securityManager.setRealm(customJwtRealm);
        // 关闭Shiro自带的session（因为我们采用的是Jwt token的机制）
        DefaultSubjectDAO defaultSubjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        defaultSubjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(defaultSubjectDAO);
        // 设置自定义Cache缓存
        securityManager.setCacheManager(new CustomCacheManager(template));

        //解决无法获取securityManager对象问题, 网上基本不是这么解决的的, 个人尝试结果(替代getSecurityManager方法)
        //SecurityUtils.setSecurityManager(securityManager);

        return securityManager;
    }

    /**
     * by _days
     * 不知道有什么屌用, 但是解决了securityManager对象丢失的问题
     */
    @Bean
    public SecurityManager getSecurityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    /**
     * 配置自定义过滤器
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(DefaultWebSecurityManager defaultWebSecurityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        // 添加自己的过滤器名为jwtFilter
        Map<String, Filter> filterMap = new HashMap<>(16);
        filterMap.put("jwtFilter", jwtFilterBean());
        factoryBean.setFilters(filterMap);
        factoryBean.setSecurityManager(defaultWebSecurityManager);
        // 设置无权限时跳转的 url;
        //factoryBean.setUnauthorizedUrl("/unauthorized/无权限");
        // 自定义url规则
        HashMap<String, String> filterRuleMap = new HashMap<>(16);
        // 所有请求通过我们自己的JwtFilter
        filterRuleMap.put("/**", "jwtFilter");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
    }

    /**
     * <pre>
     * 注入bean，此处应注意：
     *
     * (1)代码顺序，应放置于shiroFilter后面，否则报错：
     *
     * (2)如不在此注册，在filter中将无法正常注入bean
     * </pre>
     */
    @Bean("jwtFilter")
    public CustomJwtFilter jwtFilterBean() {
        return new CustomJwtFilter();
    }

    /**
     * 添加注解支持
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        // 强制使用cglib，防止重复代理和可能引起代理出错的问题，https://zhuanlan.zhihu.com/p/29161098
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * ||启动shiro的aop||
     * 使得我们后面加在方法上面的权限控制注解可以生效。
     * 例如：@RequiresPermissions("/sys/bank/delete"), @RequiresRoles("admin")
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(
            DefaultWebSecurityManager defaultWebSecurityManager
    ) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(defaultWebSecurityManager);
        return advisor;
    }

}