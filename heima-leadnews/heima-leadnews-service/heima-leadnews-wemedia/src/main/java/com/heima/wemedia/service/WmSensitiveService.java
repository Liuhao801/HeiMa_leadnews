package com.heima.wemedia.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;

/**
 * 敏感词相关接口
 */
public interface WmSensitiveService {
    /**
     * 分页查询敏感词
     * @param dto
     * @return
     */
    public ResponseResult list(WmSensitiveDto dto);

    /**
     * 新增敏感词
     * @param wmSensitive
     * @return
     */
    public ResponseResult saveSensitive(WmSensitive wmSensitive);

    /**
     * 修改敏感词
     * @param wmSensitive
     * @return
     */
    public ResponseResult updateSensitive(WmSensitive wmSensitive);

    /**
     * 删除敏感词
     * @param id
     * @return
     */
    public ResponseResult delSensitive(Integer id);
}
