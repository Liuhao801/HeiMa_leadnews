package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;

@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
    public void onMeaasge(String message){
        if(StringUtils.isNotBlank(message)){
            log.info("开始创建es索引,message:{}",message);
            //1、解析消息
            SearchArticleVo searchArticleVo = JSON.parseObject(message, SearchArticleVo.class);
            //2、添加到索引库
            IndexRequest indexRequest=new IndexRequest("app_info_article");
            indexRequest.id(searchArticleVo.getId().toString()).source(message, XContentType.JSON);
            try {
                restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            } catch (IOException e) {
                log.info("创建es索引失败,message:{}",message);
                e.printStackTrace();
            }
        }
    }


}
