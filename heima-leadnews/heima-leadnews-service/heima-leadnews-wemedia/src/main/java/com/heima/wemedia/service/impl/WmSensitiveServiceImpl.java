package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmSensitiveDto;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.service.WmSensitiveService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class WmSensitiveServiceImpl implements WmSensitiveService {
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;

    /**
     * 分页查询敏感词
     * @param dto
     * @return
     */
    @Override
    public ResponseResult list(WmSensitiveDto dto) {
        //1、检查参数
        dto.checkParam();
        //2、分页参数
        Page<WmSensitive> page =new Page<>(dto.getPage(),dto.getSize());
        //3、查询条件
        LambdaQueryWrapper<WmSensitive> queryWrapper = new LambdaQueryWrapper<WmSensitive>()
                .like(StringUtils.isNotBlank(dto.getName()),WmSensitive::getSensitives, dto.getName())  //关键词模糊查询
                .orderByDesc(WmSensitive::getCreatedTime);  //按创建时间降序
        Page<WmSensitive> result = wmSensitiveMapper.selectPage(page, queryWrapper);
        //4、封装返回结果
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) result.getTotal());
        responseResult.setData(result.getRecords());
        return responseResult;
    }

    /**
     * 新增敏感词
     * @param wmSensitive
     * @return
     */
    @Override
    @Transactional
    public ResponseResult saveSensitive(WmSensitive wmSensitive) {
        //1、检查参数
        if(StringUtils.isBlank(wmSensitive.getSensitives())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询是否存在相同敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(new LambdaQueryWrapper<WmSensitive>()
                .eq(WmSensitive::getSensitives, wmSensitive.getSensitives()));
        if(wmSensitives==null||wmSensitives.size()<=0){
            //新增敏感词
            wmSensitive.setCreatedTime(new Date());
            int insert = wmSensitiveMapper.insert(wmSensitive);
            if(insert<=0){
                log.error("新增敏感词失败,wmSensitive:{}",wmSensitive);
            }
            return ResponseResult.okResult(null);
        }else {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"敏感词已存在");
        }
    }

    /**
     * 修改敏感词
     * @param wmSensitive
     * @return
     */
    @Override
    @Transactional
    public ResponseResult updateSensitive(WmSensitive wmSensitive) {
        //1、检查参数
        if(wmSensitive.getId()==null||StringUtils.isBlank(wmSensitive.getSensitives())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询敏感词数据
        WmSensitive wmSensitiveDb = wmSensitiveMapper.selectById(wmSensitive.getId());
        if(wmSensitiveDb==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        //3、查询是否存在相同敏感词
        if(!wmSensitiveDb.getSensitives().equals(wmSensitive.getSensitives())){
            List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(new LambdaQueryWrapper<WmSensitive>()
                    .eq(WmSensitive::getSensitives, wmSensitive.getSensitives()));
            if(wmSensitives!=null&&wmSensitives.size()>0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST,"敏感词已存在");
            }
        }
        //4、修改敏感词
        int i = wmSensitiveMapper.updateById(wmSensitive);
        if(i<=0){
            log.error("修改敏感词失败,wmSensitive:{}",wmSensitive);
        }
        return ResponseResult.okResult(null);
    }

    /**
     * 删除敏感词
     * @param id
     * @return
     */
    @Override
    public ResponseResult delSensitive(Integer id) {
        //1、检查参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、删除敏感词
        wmSensitiveMapper.deleteById(id);
        return ResponseResult.okResult(null);
    }
}
