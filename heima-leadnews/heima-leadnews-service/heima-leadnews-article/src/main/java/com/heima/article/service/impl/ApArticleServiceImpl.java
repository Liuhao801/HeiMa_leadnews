package com.heima.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    private final static Short MAX_PAGE_SIZE=50;

    /**
     * 加载文章
     * @param articleHomeDto
     * @param type  1为加载更多  2为加载最新
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto articleHomeDto, Short type) {
        //1、校验参数
        //分页参数校验
        Integer size = articleHomeDto.getSize();
        if(size==null||size<=0){
            //默认查询10条数据
            size=10;
        }
        //最多查询50条数据
        size=Math.min(size,MAX_PAGE_SIZE);
        articleHomeDto.setSize(size);

        //type参数校验
        if(!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE)&&!type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            type=ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //文章频道参数校验
        if(StringUtils.isBlank(articleHomeDto.getTag())){
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        //时间参数校验
        if(articleHomeDto.getMaxBehotTime()==null)articleHomeDto.setMaxBehotTime(new Date());
        if(articleHomeDto.getMinBehotTime()==null)articleHomeDto.setMinBehotTime(new Date());

        //2、查询数据
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(articleHomeDto, type);
        //3、封装结果并返回
        return ResponseResult.okResult(apArticles);
    }
}
