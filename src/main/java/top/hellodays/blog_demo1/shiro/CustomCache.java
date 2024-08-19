package top.hellodays.blog_demo1.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.hellodays.blog_demo1.constant.ShiroConstant;
import top.hellodays.blog_demo1.utils.JwtUtil;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 重写Shiro的Cache保存读取
 */
@Component
public class CustomCache<K, V> implements Cache<K, V> {

    @Value("${config.accessToken-expireTime}")
    private String ACCESS_TOKEN_EXPIRE_TIME;

    private final RedisTemplate<String, Object> redisTemplate;

    // todo: 如果jwt的缓存除了问题，可能需要去除这里的@Autowired
    @Autowired
    public CustomCache(RedisTemplate redisTemplate) {
        // 使用StringRedisSerializer做序列化
        // redisTemplate.setValueSerializer(new StringRedisSerializer());
        this.redisTemplate = redisTemplate;
    }

    /**
     * 缓存的key名称获取为shiro:cache:account
     *
     * @param key
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/4 18:33
     */
    private String getKey(Object key) {
        return ShiroConstant.PREFIX_SHIRO_CACHE + JwtUtil.getClaim(key.toString(), ShiroConstant.USERNAME);
    }

    /**
     * 获取缓存
     */
    @Override
    public Object get(Object key) throws CacheException {
        return redisTemplate.opsForValue().get(this.getKey(key));
    }

    /**
     * 保存缓存
     */
    @Override
    public Object put(Object key, Object value) throws CacheException {
        // 读取配置文件，获取Redis的Shiro缓存过期时间
        // PropertiesUtil.readProperties("config.properties");
        // String shiroCacheExpireTime =
        // PropertiesUtil.getProperty("shiroCacheExpireTime");
        // 设置Redis的Shiro缓存
        try {
            redisTemplate.opsForValue().set(this.getKey(key), value, Integer.parseInt(ACCESS_TOKEN_EXPIRE_TIME), TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除缓存
     */
    @Override
    public Object remove(Object key) throws CacheException {
        redisTemplate.delete(this.getKey(key));
        return null;
    }

    /**
     * 清空所有缓存
     */
    @Override
    public void clear() throws CacheException {
        // TODO Auto-generated method stub

    }

    /**
     * 缓存的个数
     */
    @Override
    public Set<K> keys() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * 获取所有的key
     */
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * 获取所有的value
     */
    @Override
    public Collection<V> values() {
        // TODO Auto-generated method stub
        return null;
    }
}