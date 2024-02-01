package com.heima.user.controller.v1;

import com.heima.common.constants.UserConstants;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.ApUserAuthDto;
import com.heima.user.service.ApUserAuthService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@ApiOperation("用户实名认证相关接口")
public class ApUserAuthConrtoller {

    @Autowired
    private ApUserAuthService apUserAuthService;

    @PostMapping("/list")
    @ApiOperation("分页查询用户认证信息")
    public ResponseResult list(@RequestBody ApUserAuthDto dto){
        log.info("分页查询用户认证信息,dto:{}",dto);
        return apUserAuthService.list(dto);
    }

    @PostMapping("/authFail")
    @ApiOperation("审核失败")
    public ResponseResult authFail(@RequestBody ApUserAuthDto dto){
        log.info("审核失败,用户id:{},审核失败原因:{}",dto.getId(),dto.getMsg());
        return apUserAuthService.updateStatus(dto, UserConstants.FAIL_AUTH);
    }

    @PostMapping("/authPass")
    @ApiOperation("审核成功")
    public ResponseResult authPass(@RequestBody ApUserAuthDto dto){
        log.info("审核成功,用户id:{}",dto.getId());
        return apUserAuthService.updateStatus(dto,UserConstants.PASS_AUTH);
    }
}
