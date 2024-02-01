package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;

/**
 * 频道相关接口
 */
public interface WmChannelService {
    /**
     * 查询所有频道信息
     * @return
     */
    public ResponseResult findAll();

    /**
     * 删除频道信息
     * @return
     */
    public ResponseResult delChannel(Integer id);

    /**
     * 分页查询频道信息
     * @param dto
     * @return
     */
    public ResponseResult list(WmChannelDto dto);

    /**
     * 新增频道
     * @param wmChannel
     * @return
     */
    public ResponseResult saveChannel(WmChannel wmChannel);

    /**
     * 更新频道信息
     * @param wmChannel
     * @return
     */
    public ResponseResult updateChannel(WmChannel wmChannel);
}

