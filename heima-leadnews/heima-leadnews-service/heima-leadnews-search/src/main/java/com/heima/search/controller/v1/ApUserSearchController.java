package com.heima.search.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.search.service.ApUserSearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
@Slf4j
@Api(value = "用户搜索记录相关接口")
public class ApUserSearchController {

    @Autowired
    private ApUserSearchService apUserSearchService;

    @PostMapping("/load")
    @ApiOperation("查询用户搜索记录")
    public ResponseResult findUserSearch() {
        log.info("查询用户搜索记录");
        return apUserSearchService.findUserSearch();
    }

    @PostMapping("/del")
    @ApiOperation("删除搜索记录")
    public ResponseResult delUserSearch(@RequestBody HistorySearchDto dto) {
        log.info("删除搜索记录");
        return apUserSearchService.delUserSearch(dto);
    }
}
