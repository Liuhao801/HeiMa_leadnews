package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class WmChannelServiceImpl implements WmChannelService {
    @Autowired
    private WmChannelMapper wmChannelMapper;

    /**
     * 查询所有频道信息
     * @return
     */
    @Override
    public ResponseResult findAll() {
        List<WmChannel> wmChannels = wmChannelMapper.selectList(null);
        return ResponseResult.okResult(wmChannels);
    }
}
