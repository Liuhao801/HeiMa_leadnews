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
        FileInputStream inputStream = new FileInputStream("E:\\java\\HeiMa_leadnews\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\js\\index.js");
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .object("plugins/js/index.js")
                .contentType("text/javascript")
                .bucket("leadnews")
                .stream(inputStream,inputStream.available(),-1)
                .build();
        minioClient.putObject(putObjectArgs);
        System.out.println("上传成功");
    }
}
