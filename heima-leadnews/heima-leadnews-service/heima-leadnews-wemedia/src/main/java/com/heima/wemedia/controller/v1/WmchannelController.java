package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.wemedia.service.WmChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 自媒体频道相关接口
 */
@RestController
@RequestMapping("/api/v1/channel")
@Api(value = "频道相关接口")
@Slf4j
public class WmchannelController {

    @Autowired
    private WmChannelService wmChannelService;

    @GetMapping("/channels")
    @ApiOperation("查询所有频道信息")
    public ResponseResult findAll(){
        log.info("查询所有频道信息");
        return wmChannelService.findAll();
    }


}
