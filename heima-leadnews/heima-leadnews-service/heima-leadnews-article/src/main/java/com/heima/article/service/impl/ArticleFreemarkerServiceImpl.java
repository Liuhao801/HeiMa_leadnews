package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    @Autowired
    private Configuration configuration;
    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    /**
     * 生成静态文件上传到minIO中
     * @param articleId app端文章id
     * @param content   文章内容
     */
    @Override
    @Async
    public void buildArticleToMinIO(Long articleId, String content) {
        if(StringUtils.isNotBlank(content)){
            StringWriter out= null;
            try {
                //1、利用freemarker生成html页面
                out = new StringWriter();
                Template template = configuration.getTemplate("article.ftl");
                //准备模型数据
                Map<String,Object> map=new HashMap<>();
                map.put("content", JSON.parseArray(content));
                template.process(map,out);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //3、上传到minio
            InputStream in=new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
            String path = fileStorageService.uploadHtmlFile("", articleId + ".html", in);
            //4、修改ap_article表的url
            ApArticle article = new ApArticle();
            article.setId(articleId);
            article.setStaticUrl(path);
            apArticleMapper.updateById(article);

            //向kafka发送消息，在es中创建索引
            createArticleESIndex(articleId,content);
        }
    }

    /**
     * 向kafka发送消息，在es中创建索引
     * @param articleId
     * @param content
     */
    private void createArticleESIndex(Long articleId, String content) {
        //1、封装数据
        ApArticle apArticle = apArticleMapper.selectById(articleId);
        SearchArticleVo searchArticleVo =new SearchArticleVo();
        BeanUtils.copyProperties(apArticle,searchArticleVo);
        searchArticleVo.setContent(content);

        //2、向kafka发送消息
        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC,JSON.toJSONString(searchArticleVo));
    }
}
