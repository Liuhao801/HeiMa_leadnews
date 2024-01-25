package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
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
}
