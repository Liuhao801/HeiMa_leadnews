package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
@Api(value = "文章查询")
public class ArticleHomeController {

    @Autowired
    private ApArticleService apArticleService;

    @PostMapping("/load")
    @ApiOperation("加载首页")
    public ResponseResult load(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @PostMapping("/loadmore")
    @ApiOperation("加载更多")
    public ResponseResult loadmore(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_MORE);
    }

    @PostMapping("/loadnew")
    @ApiOperation("加载最新")
    public ResponseResult loadnew(@RequestBody ArticleHomeDto articleHomeDto){
        return apArticleService.load(articleHomeDto, ArticleConstants.LOADTYPE_LOAD_NEW);
    }
}
