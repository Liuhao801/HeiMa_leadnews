package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.thread.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WmNewsServiceImpl implements WmNewsService {
    @Autowired
    private WmNewsMapper wmNewsMapper;

    /**
     * 条件分页查询文章列表
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(WmNewsPageReqDto dto) {
        //1、检查参数
        dto.checkParam();
        //2、条件分页查询
        //分页参数
        Page<WmNews> page=new Page<>(dto.getPage(),dto.getSize());
        //查询条件
        LambdaQueryWrapper<WmNews> queryWrapper = new LambdaQueryWrapper<WmNews>()
                .eq(WmNews::getUserId, WmThreadLocalUtils.getUser().getId())  //查询当前登录用户的文章
                .eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus())  //状态精确查询
                .eq(dto.getChannelId() != null, WmNews::getChannelId, dto.getChannelId())  //频道精确查询
                .like(StringUtils.isNotBlank(dto.getKeyword()), WmNews::getTitle, dto.getKeyword()) //关键字模糊查询
                .between(dto.getBeginPubDate() != null && dto.getEndPubDate() != null, WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate())  //时间范围查询
                .orderByDesc(WmNews::getPublishTime);  //发布时间倒序查询
        Page<WmNews> result = wmNewsMapper.selectPage(page, queryWrapper);
        //3、封装返回结果
        ResponseResult responseResult=new PageResponseResult(dto.getPage(),dto.getSize(), (int) result.getTotal());
        responseResult.setData(result.getRecords());
        return responseResult;
    }
}
