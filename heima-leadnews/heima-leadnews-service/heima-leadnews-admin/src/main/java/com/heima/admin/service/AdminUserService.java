package com.heima.admin.service;

import com.heima.model.admin.dtos.AdminLoginDto;
import com.heima.model.common.dtos.ResponseResult;

/**
 * 管理端用户相关接口
 */
public interface AdminUserService {

    /**
     * 管理端用户登录
     * @param dto
     * @return
     */
    public ResponseResult login(AdminLoginDto dto);
}
