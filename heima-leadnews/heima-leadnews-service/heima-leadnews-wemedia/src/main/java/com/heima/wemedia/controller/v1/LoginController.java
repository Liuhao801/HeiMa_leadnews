package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.wemedia.service.WmUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Api(value = "自媒体端用户登录")
@Slf4j
public class LoginController {

    @Autowired
    private WmUserService wmUserService;

    @PostMapping("/in")
    @ApiOperation("自媒体端用户登录")
    public ResponseResult login(@RequestBody WmLoginDto dto){
        log.info("自媒体端用户登录,dto:{}",dto);
        return wmUserService.login(dto);
    }
}
