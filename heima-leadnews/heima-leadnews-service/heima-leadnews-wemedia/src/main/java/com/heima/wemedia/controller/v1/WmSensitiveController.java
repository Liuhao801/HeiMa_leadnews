package com.heima.wemedia.controller.v1;

import com.heima.wemedia.service.WmSensitiveService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensitive")
@Slf4j
@Api(value = "敏感词相关接口")
public class WmSensitiveController {
    @Autowired
    private WmSensitiveService adminSensitiveService;

    @PostMapping("/list")
    @ApiOperation("分页查询敏感词")
    public ResponseResult list(@RequestBody WmSensitiveDto dto){
        log.info("分页查询敏感词,dto:{}",dto);
        return  adminSensitiveService.list(dto);
    }

    @PostMapping("/save")
    @ApiOperation("新增敏感词")
    public ResponseResult saveSensitive(@RequestBody WmSensitive wmSensitive){
        log.info("新增敏感词,wmSensitive:{}",wmSensitive);
        return  adminSensitiveService.saveSensitive(wmSensitive);
    }

    @PostMapping("/update")
    @ApiOperation("修改敏感词")
    public ResponseResult updateSensitive(@RequestBody WmSensitive wmSensitive){
        log.info("修改敏感词,wmSensitive:{}",wmSensitive);
        return  adminSensitiveService.updateSensitive(wmSensitive);
    }

    @DeleteMapping("/del/{id}")
    @ApiOperation("删除敏感词")
    public ResponseResult delSensitive(@PathVariable Integer id){
        log.info("删除敏感词,敏感词id:{}",id);
        return  adminSensitiveService.delSensitive(id);
    }
}
