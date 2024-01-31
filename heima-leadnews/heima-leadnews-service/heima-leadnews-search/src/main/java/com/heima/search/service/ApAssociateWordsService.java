package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

/**
 * 联想词相关接口
 */
public interface ApAssociateWordsService {

    /**
     * 查询联想词
     * @param dto
     * @return
     */
    public ResponseResult findAssociate(UserSearchDto dto);

}