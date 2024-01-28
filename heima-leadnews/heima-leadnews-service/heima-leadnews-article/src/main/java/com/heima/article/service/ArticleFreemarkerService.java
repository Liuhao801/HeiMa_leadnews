package com.heima.article.service;

import com.heima.model.article.pojos.ApArticle;

public interface ArticleFreemarkerService {

    /**
     * 生成静态文件上传到minIO中
     * @param articleId app端文章id
     * @param content 文章内容
     */
    public void buildArticleToMinIO(Long articleId,String content);
}