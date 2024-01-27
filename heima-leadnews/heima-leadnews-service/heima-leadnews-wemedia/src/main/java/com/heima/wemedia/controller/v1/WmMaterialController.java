package com.heima.wemedia.controller.v1;

import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 自媒体素材相关接口
 */
@RestController
@RequestMapping("/api/v1/material")
@Api(value = "自媒体素材相关接口")
@Slf4j
public class WmMaterialController {

    @Autowired
    private WmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    @ApiOperation("上传图片素材")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        log.info("上传图片素材,multipartFile:{}",multipartFile.getOriginalFilename());
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    @ApiOperation("图片列表查询")
    public ResponseResult list(@RequestBody WmMaterialDto dto){
        log.info("图片列表查询,dto:{}",dto);
        return wmMaterialService.list(dto);
    }

    @GetMapping("del_picture/{id}")
    @ApiOperation("删除图片")
    public ResponseResult delPicture(@PathVariable Integer id){
        log.info("删除图片,图片id:{}",id);
        return wmMaterialService.delPicture(id);
    }

    @GetMapping("collect/{id}")
    @ApiOperation("收藏图片")
    public ResponseResult collect(@PathVariable Integer id){
        log.info("收藏图片,图片id:{}",id);
        return wmMaterialService.change_collect(id, WemediaConstants.COLLECT_MATERIAL);
    }

    @GetMapping("cancel_collect/{id}")
    @ApiOperation("取消收藏图片")
    public ResponseResult cancelCollect(@PathVariable Integer id){
        log.info("取消收藏图片,图片id:{}",id);
        return wmMaterialService.change_collect(id, WemediaConstants.CANCEL_COLLECT_MATERIAL);
    }
}
