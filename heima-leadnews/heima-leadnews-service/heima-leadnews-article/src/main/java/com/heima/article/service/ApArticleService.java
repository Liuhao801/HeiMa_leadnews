package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleService extends IService<ApArticle> {

    /**
     * 加载文章
     * @param dto
     * @param type 1为加载更多  2为加载最新
     * @return
     */
    public ResponseResult load(ArticleHomeDto dto,Short type);
}
