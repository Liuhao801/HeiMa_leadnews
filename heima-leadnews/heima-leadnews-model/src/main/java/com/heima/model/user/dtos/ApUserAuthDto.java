package com.heima.model.user.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class ApUserAuthDto extends PageRequestDto {

    /**
     * 状态
         0 创建中
         1 待审核
         2 审核失败
         9 审核通过
     */
    private Short status;

    /**
     * 用户id
     */
    private Integer id;

    /**
     * 审核失败原因
     */
    private String msg;
}
