package com.heima.article;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;

public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://43.137.8.13:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();
    //上传文件
    @Test
    public void upload() throws Exception {
        FileInputStream inputStream =
                new FileInputStream("E:\\java\\HeiMa_leadnews\\day08-平台管理[实战]\\资料\\后台数据图\\live_image.jpg");
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object("images/live_image.jpg")
                .contentType("image/jpg")
                .bucket("leadnews")
                .stream(inputStream,inputStream.available(),-1)
                .build();
        minioClient.putObject(putObjectArgs);
        System.out.println("上传成功");
    }
}
