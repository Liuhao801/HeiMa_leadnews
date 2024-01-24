package com.heima.article;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class ArticleFreemarkerTest {

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void createStaticUrlTest() throws IOException, TemplateException {
        //1、获取文章内容
        Long id=1383827787629252610L;
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(new LambdaQueryWrapper<ApArticleContent>()
                .eq(ApArticleContent::getArticleId, id));
        if(apArticleContent!=null&& StringUtils.isNotBlank(apArticleContent.getContent())){
            //2、利用freemarker生成html页面
            StringWriter out=new StringWriter();
            Template template = configuration.getTemplate("article.ftl");
            //准备模型数据
            Map<String,Object>map=new HashMap<>();
            map.put("content", JSON.parseArray(apArticleContent.getContent()));
            template.process(map,out);
            //3、上传到minio
            InputStream in=new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", in);
            //4、修改ap_article表的url
            ApArticle article = new ApArticle();
            article.setId(apArticleContent.getArticleId());
            article.setStaticUrl(path);
            apArticleMapper.updateById(article);
        }

    }
}
