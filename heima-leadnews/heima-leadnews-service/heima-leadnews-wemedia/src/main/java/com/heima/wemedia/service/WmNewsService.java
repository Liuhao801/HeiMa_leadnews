package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;

/**
 * 自媒体文章相关接口
 */
public interface WmNewsService {

    /**
     * 条件分页查询文章列表
     * @param dto
     * @return
     */
    public ResponseResult list( WmNewsPageReqDto dto);

    /**
     * 保存修改文章或保存草稿
     * @param dto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto dto);

    /**
     * 查询文章详情
     * @param id 文章id
     * @return
     */
    public ResponseResult getNews(Integer id);

    /**
     * 删除文章
     * @param id 文章id
     * @return
     */
    public ResponseResult delNews(Integer id);

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDto dto);
}
