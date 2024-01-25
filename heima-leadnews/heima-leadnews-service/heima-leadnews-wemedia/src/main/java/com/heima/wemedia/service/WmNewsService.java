package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 自媒体文章相关接口
 */
public interface WmNewsService {

    /**
     * 条件分页查询文章列表
     * @param dto
     * @return
     */
    public ResponseResult list(@RequestBody WmNewsPageReqDto dto);
}
