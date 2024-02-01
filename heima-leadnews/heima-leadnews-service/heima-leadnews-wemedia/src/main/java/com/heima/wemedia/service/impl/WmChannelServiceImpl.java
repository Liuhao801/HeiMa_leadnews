package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmChannelDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmChannelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class WmChannelServiceImpl implements WmChannelService {
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmNewsMapper wmNewsMapper;

    /**
     * 查询所有频道信息
     * @return
     */
    @Override
    public ResponseResult findAll() {
        List<WmChannel> wmChannels = wmChannelMapper.selectList(null);
        return ResponseResult.okResult(wmChannels);
    }

    /**
     * 删除频道信息
     * @return
     */
    @Override
    public ResponseResult delChannel(Integer id) {
        //1、检查参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、只有禁用的频道可以删除
        WmChannel wmChannel = wmChannelMapper.selectById(id);
        if(wmChannel==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"频道不存在");
        }
        if(wmChannel.getStatus()){
           return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前频道正在启用，不能删除");
        }
        //3、删除频道信息
        wmChannelMapper.deleteById(id);
        return ResponseResult.okResult(null);
    }

    /**
     * 分页查询频道信息
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(WmChannelDto dto) {
        //1、检查参数
        dto.checkParam();
        //2、分页参数
        Page<WmChannel> page = new Page<>(dto.getPage(), dto.getSize());
        //3、查询条件
        LambdaQueryWrapper<WmChannel> queryWrapper = new LambdaQueryWrapper<WmChannel>()
                .like(StringUtils.isNotBlank(dto.getName()), WmChannel::getName, dto.getName())  //关键词模糊查询
                .orderByDesc(WmChannel::getCreatedTime);  //按创建时间降序
        Page<WmChannel> result = wmChannelMapper.selectPage(page, queryWrapper);
        //4、封装返回结果
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) result.getTotal());
        responseResult.setData(result.getRecords());
        return responseResult;
    }

    /**
     * 新增频道
     * @param wmChannel
     * @return
     */
    @Override
    @Transactional
    public ResponseResult saveChannel(WmChannel wmChannel) {
        //1、检查参数
        if(StringUtils.isBlank(wmChannel.getName())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询是否存在相同频道
        List<WmChannel> wmChannels = wmChannelMapper.selectList(new LambdaQueryWrapper<WmChannel>()
                .eq(WmChannel::getName, wmChannel.getName()));
        if(wmChannels==null||wmChannels.size()<=0){
            //新增频道
            wmChannel.setCreatedTime(new Date());
            wmChannel.setIsDefault(true);  //默认频道
            int insert = wmChannelMapper.insert(wmChannel);
            if(insert<=0){
                log.error("新增频道失败,wmChannel:{}",wmChannel);
            }
            return ResponseResult.okResult(null);
        }else {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"频道已存在");
        }
    }

    /**
     * 更新频道信息
     * @param wmChannel
     * @return
     */
    @Override
    @Transactional
    public ResponseResult updateChannel(WmChannel wmChannel) {
        //1、检查参数
        if(wmChannel.getId()==null||StringUtils.isBlank(wmChannel.getName())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询频道数据
        WmChannel wmChannelDb = wmChannelMapper.selectById(wmChannel.getId());
        if(wmChannelDb==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //3、查询是否存在相同频道
        if(!wmChannelDb.getName().equals(wmChannel.getName())){
            List<WmChannel> wmChannels = wmChannelMapper.selectList(new LambdaQueryWrapper<WmChannel>()
                    .eq(WmChannel::getName, wmChannel.getName()));
            if(wmChannels!=null&&wmChannels.size()>0){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"频道已存在");
            }
        }

        //4、如果频道被引用则不能禁用
        if(!wmChannel.getStatus()){
            //查询是否有引用的文章
            List<WmNews> wmNews = wmNewsMapper.selectList(new LambdaQueryWrapper<WmNews>()
                    .eq(WmNews::getChannelId, wmChannel.getId()));
            if(wmNews!=null&&wmNews.size()>0){
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"当前频道被引用，不能禁用");
            }
        }

        int i = wmChannelMapper.updateById(wmChannel);
        if(i<=0){
            log.info("更新频道信息失败,wmChannel:{}",wmChannel);
        }
        return ResponseResult.okResult(null);
    }
}
