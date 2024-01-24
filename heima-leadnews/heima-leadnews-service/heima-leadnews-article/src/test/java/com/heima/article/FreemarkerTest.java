package com.heima.article;

import com.alibaba.fastjson.JSON;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class FreemarkerTest {

    @Autowired
    private Configuration configuration;

    @Test
    public void test() throws IOException, TemplateException {
        //freemarker的模板对象，获取模板
        Template template = configuration.getTemplate("article.ftl");
        //准备模型数据
        String content="[{\"type\":\"text\",\"value\":\"Kafka机制\"},{\"type\":\"image\",\"value\":\"http://43.137.8.13:9000/leadnews/1.jpg\"},{\"type\":\"text\",\"value\":\"hhh\"}]";
        Map<String,Object>params=new HashMap<>();
        params.put("content", JSON.parseArray(content));
        template.process(params, new FileWriter("e:/list.html"));
    }
}
