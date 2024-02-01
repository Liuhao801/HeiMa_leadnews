package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.admin.mapper.AdminUserMapper;
import com.heima.admin.service.AdminUserService;
import com.heima.model.admin.dtos.AdminLoginDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    /**
     * 管理端用户登录
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(AdminLoginDto dto) {
        //1、检查参数
        if(dto==null || StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"用户名或密码为空");
        }
        //2、查询用户
        AdUser adUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdUser>().eq(AdUser::getName, dto.getName()));
        if(adUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"用户不存在");
        }
        //3、校验加盐密码
        String salt = adUser.getSalt();
        //生成加密后密码
        String password = DigestUtils.md5DigestAsHex((dto.getPassword() + salt).getBytes());
        if(password.equals(adUser.getPassword())){
            //4、生成token
            String token = AppJwtUtil.getToken(adUser.getId().longValue());
            //5、封装返回结果
            Map<String,Object> map = new HashMap<>();
            adUser.setPassword("");
            adUser.setSalt("");
            map.put("user",adUser);
            map.put("token",token);
            return ResponseResult.okResult(map);
        }else{
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }

    }
}
