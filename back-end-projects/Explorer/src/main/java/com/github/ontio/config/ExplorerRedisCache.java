package com.github.ontio.config;

import com.github.ontio.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author zhouq
 * @version 1.0
 * @date 2018/7/16
 */
@Component
@Slf4j
public class ExplorerRedisCache implements Cache {
    
    // 读写锁
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

    private RedisTemplate<String, Object> redisTemplate = ApplicationContextProvider.getBean("redisTemplate");

    //private RedisTemplate redisTemplate;

    private String id = "defaultrediscacheid001";

    public ExplorerRedisCache(final String id) {

        log.info("##init ExplorerRedisCache, Cache id:{}##", id);
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    public ExplorerRedisCache() {
        log.info("##init ExplorerRedisCache with default Cache id:{}##", this.id);
    }

    @Override
    public String getId() {
        log.info("##get Redis Cache Id:{}##", this.id);
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        log.info("##putObject. key:{}, value:{}##", key, value);
        //RedisTemplate redisTemplate = getRedisTemplate();

        if (value != null) {
            // 向Redis中添加数据，有效时间是2天
           // redisTemplate.opsForValue().set(key.toString(), JSONObject.toJSONString(value), 1, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(key.toString(), value, 3, TimeUnit.MINUTES);
        }
    }

    @Override
    public Object getObject(Object key) {
        log.info("##getObject. key:{}##", key.toString());
        try {
            //RedisTemplate redisTemplate = getRedisTemplate();
            if (key != null) {
/*                String obj = (String)redisTemplate.opsForValue().get(key.toString());
                return  JSONArray.parseArray(obj);*/
/*                if(obj instanceof JSONArray){
                    return JSONArray.parseArray((String) obj);
                }else if(obj instanceof JSONObject){
                    return JSONObject.parseObject((String) obj);
                }*/
                Object obj = redisTemplate.opsForValue().get(key.toString());
                return  obj;
                //log.info("redis value:{}", JacksonUtil.beanToJSonStr(obj));
            }
        } catch (Exception e) {
            log.error("redis error... ", e);
        }
        return null;
    }

    /**
     * 批量删除对应的value
     * @param keys
     */
    public void remove(final String... keys) {
        for (String key : keys) {
            removeObject(key);
        }
    }


    @Override
    public Object removeObject(Object key) {
        //RedisTemplate redisTemplate = getRedisTemplate();

        log.info("##removeObject. key:{}##", key.toString());
        try {
            if (key != null) {
                redisTemplate.delete(key.toString());
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void clear() {
        log.info("clear Redis Cache,this.id:{}",this.id);
        //RedisTemplate redisTemplate = getRedisTemplate();

        try {
            Set<String> keys = redisTemplate.keys("*:" + this.id + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                log.info("keys:{}",keys);
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public int getSize() {
        log.info("##get Redis Cache Size##");
        Long size = (Long) redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize();
            }
        });
        return size.intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        log.info("##get Redis Cache ReadWriteLock##");
        return this.readWriteLock;
    }

/*    private RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = ApplicationContextProvider.getBean("redisTemplate");
        }
        return redisTemplate;
    }*/


}