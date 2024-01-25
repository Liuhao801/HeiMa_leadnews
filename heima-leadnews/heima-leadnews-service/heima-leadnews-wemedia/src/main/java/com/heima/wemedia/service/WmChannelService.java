package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;

/**
 * 频道相关接口
 */
public interface WmChannelService {
    /**
     * 查询所有频道信息
     * @return
     */
    public ResponseResult findAll();
}

