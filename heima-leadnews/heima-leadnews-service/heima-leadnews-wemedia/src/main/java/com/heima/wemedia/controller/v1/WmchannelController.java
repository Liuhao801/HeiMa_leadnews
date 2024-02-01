package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
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

    @GetMapping("/del/{id}")
    @ApiOperation("删除频道信息")
    public ResponseResult delChannel(@PathVariable Integer id){
        log.info("删除频道信息,频道id:{}",id);
        return wmChannelService.delChannel(id);
    }

    @PostMapping("/list")
    @ApiOperation("分页查询频道信息")
    public ResponseResult list(@RequestBody WmChannelDto dto){
        log.info("分页查询频道信息,dto:{}",dto);
        return wmChannelService.list(dto);
    }
    @PostMapping("/save")
    @ApiOperation("新增频道")
    public ResponseResult saveChannel(@RequestBody WmChannel wmChannel){
        log.info("新增频道,wmChannel:{}",wmChannel);
        return wmChannelService.saveChannel(wmChannel);
    }

    @PostMapping("/update")
    @ApiOperation("更新频道信息")
    public ResponseResult updateChannel(@RequestBody WmChannel wmChannel){
        log.info("更新频道信息wmChannel:{}",wmChannel);
        return wmChannelService.updateChannel(wmChannel);
    }


}
