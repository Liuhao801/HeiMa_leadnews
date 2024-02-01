package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.model.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmLoginDto dto);

    /**
     * 根据用户名获取用户信息
     * @param name
     * @return
     */
    public WmUser findWmUserByName(String name);

    /**
     * 保存自媒体端用户
     * @param wmUser
     * @return
     */
    public ResponseResult saveWmUser(WmUser wmUser);
}