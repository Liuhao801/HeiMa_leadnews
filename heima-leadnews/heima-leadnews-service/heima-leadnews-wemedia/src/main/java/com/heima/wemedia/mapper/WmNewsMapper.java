package com.heima.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.wemedia.dtos.WmNewsAuthDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.vos.WmNewsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WmNewsMapper extends BaseMapper<WmNews> {
    /**
     * 分页查询
     * @param dto
     * @return
     */
    List<WmNewsVo> findListAndPage(@Param("dto")WmNewsAuthDto dto);

    /**
     * 分页查询总记录数
     * @param dto
     * @return
     */
    int findListCount(@Param("dto")WmNewsAuthDto dto);
}
