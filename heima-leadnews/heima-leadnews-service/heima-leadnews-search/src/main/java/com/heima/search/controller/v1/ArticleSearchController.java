package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/article/search")
@Api(value = "es检索相关接口")
@Slf4j
public class ArticleSearchController {

    @Autowired
    private ArticleSearchService articleSearchService;

    @ApiOperation("es文章分页检索")
    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto dto){
        log.info("es文章分页检索，dto:{}",dto);
        return articleSearchService.search(dto);
    }
}