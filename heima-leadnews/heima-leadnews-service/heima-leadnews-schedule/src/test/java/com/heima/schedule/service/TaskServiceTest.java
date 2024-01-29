package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void addTaskTest() {
        for (int i = 0; i < 5; i++) {
            Task task = new Task();
            task.setTaskType(100+i);
            task.setPriority(100);
            task.setExecuteTime(new Date().getTime()+20000);
            task.setParameters("task_test".getBytes());

            taskService.addTask(task);
        }
    }

    @Test
    public void removeTaskTest(){
        boolean b = taskService.cancelTask(1751867390987792385L);
        System.out.println("b = " + b);
    }

    @Test
    public void popTaskTest(){
        Task task = taskService.poll(100, 100);
        System.out.println("task = " + task);
    }
}