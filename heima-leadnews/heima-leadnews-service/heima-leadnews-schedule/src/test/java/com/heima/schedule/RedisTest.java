package com.heima.schedule;

import com.alibaba.fastjson.JSON;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.sun.istack.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;

import java.util.Date;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class RedisTest {
    @Autowired
    private CacheService cacheService;

    @Test
    public void listTest(){
        //向list左边插入 key value
        cacheService.lLeftPush("list_001","hello");
        //从list右边取出
        String result = cacheService.lRightPop("list_001");
        System.out.println("result = " + result);
    }

    @Test
    public void zsetTest(){
        //向zset中插入 key value score
        cacheService.zAdd("zset_001","zset_key_001",1000);
        cacheService.zAdd("zset_001","zset_key_002",2000);
        cacheService.zAdd("zset_001","zset_key_003",4000);
        cacheService.zAdd("zset_001","zset_key_004",3000);
        //按分值取出
        Set<String> set = cacheService.zRangeByScore("zset_001", 0, 3000);
        System.out.println("set = " + set);
    }

    //耗时6151
    @Test
    public  void testPiple1(){
        long start =System.currentTimeMillis();
        for (int i = 0; i <10000 ; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }
        System.out.println("耗时"+(System.currentTimeMillis()- start));
    }


    @Test
    public void testPiple2(){
        long start  = System.currentTimeMillis();
        //使用管道技术
        List<Object> objectList = cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                for (int i = 0; i <10000 ; i++) {
                    Task task = new Task();
                    task.setTaskType(1001);
                    task.setPriority(1);
                    task.setExecuteTime(new Date().getTime());
                    redisConnection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
                }
                return null;
            }
        });
        System.out.println("使用管道技术执行10000次自增操作共耗时:"+(System.currentTimeMillis()-start)+"毫秒");
    }

}
