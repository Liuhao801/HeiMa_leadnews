package com.heima.admin.controller.v1;

import com.heima.admin.service.AdminUserService;
import com.heima.model.admin.dtos.AdminLoginDto;
import com.heima.model.common.dtos.ResponseResult;
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
@Slf4j
@Api(value = "管理端登录相关接口")
public class LoginController {

    @Autowired
    private AdminUserService adminUserService;

    @PostMapping("/in")
    @ApiOperation("管理端用户登录")
    public ResponseResult login(@RequestBody AdminLoginDto dto){
        log.info("管理端用户登录，dto:{}",dto);
        return adminUserService.login(dto);
    }
}
