package com.heima.wemedia;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.file.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class AliyunTest {
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void imageScanTest() throws Exception {
        byte[] bytes = fileStorageService.downLoadFile("http://43.137.8.13:9000/leadnews/2024/01/25/fc2d2d07ed43462f93a29b5de07c4058.png");
        List<byte[]> list=new ArrayList<>();
        list.add(bytes);
        Map map = greenImageScan.imageScan(list);
        System.out.println(map);
    }

    @Test
    public void textScanTest() throws Exception {
        Map map = greenTextScan.greeTextScan("哈哈哈病毒哈哈");
        System.out.println(map);
    }
}
