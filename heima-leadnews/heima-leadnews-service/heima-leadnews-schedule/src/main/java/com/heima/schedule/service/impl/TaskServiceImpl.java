package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private CacheService cacheService;

    /**
     * 添加任务
     * @param task 任务对象
     * @return 任务id
     */
    @Override
    @Transactional
    public Long addTask(Task task) {
        //1、将任务添加到数据库
        boolean success = addTaskToDb(task);
        if(success){
            //2、将任务添加到redis
            addTaskToRedis(task);
        }
        //3、返回任务id
        return task.getTaskId();
    }

    /**
     * 添加任务到数据库
     * @param task
     * @return
     */
    private boolean addTaskToDb(Task task) {
        boolean flag=false;

        try {
            //1、将任务添加到任务表
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task,taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);
            //2、设置任务iD
            task.setTaskId(taskinfo.getTaskId());
            //3、将任务添加到任务日志表
            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(taskinfo,taskinfoLogs);
            taskinfoLogs.setVersion(1);  //版本号
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);  //任务状态，默认0
            taskinfoLogsMapper.insert(taskinfoLogs);

            flag=true;
        } catch (BeansException e) {
            log.error("添加任务到任务表失败,task:{}",task);
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 添加任务到redis
     * @param task
     */
    private void addTaskToRedis(Task task) {
        //1、设置任务的key
        String key = task.getTaskType() + "_" + task.getPriority();
        //2、获取5分钟后的毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, ScheduleConstants.FUTURE_TIME);
        long nextScheduleTime = calendar.getTimeInMillis();

        //3、将任务存入redis
        long taskExecuteTime = task.getExecuteTime();
        if(taskExecuteTime<=System.currentTimeMillis()){
            //若任务的执行时间<=当前时间，将任务插入list
            cacheService.lLeftPush(ScheduleConstants.TOPIC+key, JSON.toJSONString(task));
        }else if(taskExecuteTime<=nextScheduleTime){
            //若任务的执行时间>当前时间&&<=预设时间(未来5分钟),将任务插入zset
            cacheService.zAdd(ScheduleConstants.FUTURE+key,JSON.toJSONString(task),taskExecuteTime);
        }
    }

    /**
     * 取消任务
     * @param taskId 任务id
     * @return 取消结果
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        //1、删除任务，更新任务日志
        Task task = updateDb(taskId,ScheduleConstants.CANCELLED);
        //2、删除redis中的任务
        if(task != null){
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    /**
     * 删除任务，更新任务日志
     * @param taskId 任务id
     * @param status 更新的状态
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            //1、删除任务表
            taskinfoMapper.deleteById(taskId);
            //2、更新任务日志表
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);
            //3、返回结果
            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs,task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        } catch (BeansException e) {
            log.info("删除任务，更新任务日志失败，taskId:{}",taskId);
            e.printStackTrace();
        }
        return task;
    }

    /**
     * 删除redis中的任务
     * @param task
     */
    private void removeTaskFromCache(Task task) {
        //1、设置任务的key
        String key = task.getTaskType() + "_" + task.getPriority();
        //2、获取5分钟后的毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, ScheduleConstants.FUTURE_TIME);
        long nextScheduleTime = calendar.getTimeInMillis();

        //3、删除redis中的任务
        long taskExecuteTime = task.getExecuteTime();
        if(taskExecuteTime<=System.currentTimeMillis()){
            //若任务的执行时间<=当前时间，从list中删除
            cacheService.lRemove(ScheduleConstants.TOPIC+key,0,JSON.toJSONString(task));
        }else if(taskExecuteTime<=nextScheduleTime){
            //若任务的执行时间>当前时间&&<=预设时间(未来5分钟),从zset中删除
            cacheService.zRemove(ScheduleConstants.FUTURE+key,JSON.toJSONString(task));
        }
    }

    /**
     * 按照类型和优先级来拉取任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        try {
            //1、从redis中拉取任务
            String key = type + "_" + priority;
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC+key);
            if(StringUtils.isNotBlank(taskJson)){
                task = JSON.parseObject(taskJson, Task.class);
                //2、删除任务，更新任务日志
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            log.error("按照类型和优先级来拉取任务失败，type:{},priority:{}",type,priority);
            e.printStackTrace();
        }
        return task;
    }

    /**
     * redis定时刷新，将到期的任务从zset同步到list
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
        //0、尝试获取分布式锁
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);
        if(StringUtils.isNotBlank(token)){
            log.info("开始定时刷新延迟任务...");
            //1、获取zset中所有的key future_*
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            futureKeys.forEach(future_key->{
                //2、得到对应的topic_key
                String topic_key = ScheduleConstants.TOPIC+future_key.split(ScheduleConstants.FUTURE)[1];
                //3、获取所有需要同步的任务
                Set<String> tasks = cacheService.zRangeByScore(future_key, 0, System.currentTimeMillis());
                if(!tasks.isEmpty()){
                    cacheService.refreshWithPipeline(future_key,topic_key,tasks);
                    log.info("成功的将" + future_key + "下的当前需要执行的任务数据刷新到" + topic_key + "下");
                }
            });
        }
    }

    /**
     * 定时将数据库中的任务添加到redis
     * 每5分钟一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    public void reloadData() {
        log.info("数据库数据开始同步到缓存...");
        //1、清理redis中的缓存数据
        clearCache();
        //2、查询执行时间小于未来5分钟的任务
        //获取5分钟后的毫秒值
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, ScheduleConstants.FUTURE_TIME);

        List<Taskinfo> taskinfos = taskinfoMapper.selectList(new LambdaQueryWrapper<Taskinfo>()
                .lt(Taskinfo::getExecuteTime, calendar.getTime()));
        //3、将任务添加到redis
        for (Taskinfo taskinfo : taskinfos) {
            Task task=new Task();
            BeanUtils.copyProperties(taskinfo,task);
            task.setExecuteTime(taskinfo.getExecuteTime().getTime());
            addTaskToRedis(task);
        }
        log.info("数据库数据同步到缓存成功");
    }

    /**
     * 清理redis中的缓存数据
     */
    private void clearCache(){
        Set<String> future_keys = cacheService.scan(ScheduleConstants.FUTURE + "*");
        Set<String> topic_keys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        cacheService.delete(future_keys);
        cacheService.delete(topic_keys);
    }
}
