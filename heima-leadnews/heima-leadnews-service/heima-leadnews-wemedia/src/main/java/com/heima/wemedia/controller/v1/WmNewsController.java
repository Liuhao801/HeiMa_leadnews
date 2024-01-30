package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.wemedia.service.WmNewsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 自媒体文章相关接口
 */
@RestController
@RequestMapping("/api/v1/news")
@Api(value = "自媒体文章相关接口")
@Slf4j
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    @PostMapping("/list")
    @ApiOperation("条件分页查询文章列表")
    public ResponseResult list(@RequestBody WmNewsPageReqDto dto){
        log.info("条件分页查询文章列表,dto:{}",dto);
        return wmNewsService.list(dto);
    }

    @PostMapping("/submit")
    @ApiOperation("保存修改文章或保存草稿")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto){
        log.info("保存修改文章或保存草稿,dto:{}",dto);
        return wmNewsService.submitNews(dto);
    }

    @GetMapping("/one/{id}")
    @ApiOperation("查询文章详情")
    public ResponseResult getNews(@PathVariable Integer id){
        log.info("查询文章详情,文章id:{}",id);
        return wmNewsService.getNews(id);
    }

    @GetMapping("/del_news/{id}")
    @ApiOperation("删除文章")
    public ResponseResult delNews(@PathVariable Integer id){
        log.info("删除文章,文章id:{}",id);
        return wmNewsService.delNews(id);
    }

    @PostMapping("/down_or_up")
    @ApiOperation("文章上下架")
    public ResponseResult downOrUp(@RequestBody WmNewsDto dto){
        log.info("文章上下架,dto:{}",dto);
        return wmNewsService.downOrUp(dto);
    }
}
