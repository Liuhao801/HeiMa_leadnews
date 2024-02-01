package com.heima.user.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.ApUserAuthDto;

/**
 * 用户认证相关接口
 */
public interface ApUserAuthService {
    /**
     * 分页查询用户认证信息
     * @param dto
     * @return
     */
    public ResponseResult list(ApUserAuthDto dto);

    /**
     * 修改审核状态
     * @param dto
     * @param status
     * @return
     */
    public ResponseResult updateStatus(ApUserAuthDto dto,Short status);

}
