package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import org.springframework.web.multipart.MultipartFile;

/**
 * 自媒体素材相关接口
 */
public interface WmMaterialService {
    /**
     * 上传图片素材
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 查询图片列表
     * @param dto
     * @return
     */
    public ResponseResult list(WmMaterialDto dto);

    /**
     * 删除图片
     * @param id 图片素材id
     * @return
     */
    public ResponseResult delPicture(Integer id);

    /**
     * 取消或添加收藏
     * @param id 图片素材id
     * @param type 1收藏 0取消收藏
     * @return
     */
    public ResponseResult change_collect(Integer id, Short type);

}
