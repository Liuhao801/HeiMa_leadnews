package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.thread.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsServiceImpl implements WmNewsService {
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsTaskService wmNewsTaskService;
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

    /**
     * 保存修改文章或保存草稿
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResponseResult submitNews(WmNewsDto dto) {
        //1、检查参数
        if(dto==null||dto.getContent().isEmpty()){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2、保存或修改文章
        //2.1、提取文章内容图片信息
        List<String> materials = ectractUrlInfo(dto.getContent());
        //2.2、如果是自动封面，获取封面图片信息
        if(WemediaConstants.WM_NEWS_TYPE_AUTO.equals(dto.getType())){
            getImagesList(dto,materials);
        }
        //2.3、保存或新增
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews);
        wmNews.setUserId(WmThreadLocalUtils.getUser().getId());  //用户Id
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1);  //默认上架

        List<String> images = dto.getImages();
        if(images!=null&&images.size()>0){
            //将List转为String
            String imagesStr = StringUtils.join(images, ",");
            wmNews.setImages(imagesStr);
        }
        if(wmNews.getId()==null){
            //新增
            int insert = wmNewsMapper.insert(wmNews);
            if(insert<=0){
                log.error("新增文章失败,wmNews：{}",wmNews);
            }
        }else{
            //修改
            int i = wmNewsMapper.updateById(wmNews);
            if(i<=0){
                log.error("修改文章失败,wmNews：{}",wmNews);
            }
            //删除素材关联表中的数据
            wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>()
                    .eq(WmNewsMaterial::getNewsId,wmNews.getId()));
        }

        //3、如果为草稿，直接返回
        if(dto.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(null);
        }
        Integer newsId = wmNews.getId();
        //4、保存文章和内容图片的关联信息
        saveRelativeInfo(materials,newsId,WemediaConstants.WM_CONTENT_REFERENCE);
        //5、保存文章和封面图片的关联信息
        saveRelativeInfo(images,newsId,WemediaConstants.WM_COVER_REFERENCE);

//        //审核文章
//        wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        //创建文章发布任务，加入延迟队列
        wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());

        return ResponseResult.okResult(null);
    }

    /**
     * 提取文章内容中的图片信息
     * @param content
     * @return
     */
    private List<String> ectractUrlInfo(String content){
        List<String> materials=new ArrayList<>();
        List<Map> maps = JSON.parseArray(content, Map.class);
        maps.forEach(map -> {
            if(WemediaConstants.WM_NEWS_TYPE_IMAGE.equals(map.get("type"))){
                materials.add((String) map.get("value"));
            }
        });
        return materials;
    }

    /**
     * 处理封面图片信息
     * @param dto
     * @param materials 内容图片列表
     * @return
     * 如果当前封面类型为自动，则设置封面类型的数据
         * 匹配规则：
         * 1，如果内容图片大于等于1，小于3  单图  type 1
         * 2，如果内容图片大于等于3  多图  type 3
         * 3，如果内容没有图片，无图  type 0
     */
    private void getImagesList(WmNewsDto dto,List<String> materials) {
        if(materials.size()>=3){
            //多图
            dto.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
            dto.setImages(materials.stream().limit(3).collect(Collectors.toList()));
        }else if(materials.size()>=1){
            //单图
            dto.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
            dto.setImages(materials.stream().limit(1).collect(Collectors.toList()));
        }else{
            //无图
            dto.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
        }
    }

    /**
     * 保存文章图片与素材的关系到数据库中
     * @param materials 图片url列表
     * @param newsId 文章id
     * @param type 图片类型
     */
    private void saveRelativeInfo(List<String> materials,Integer newsId,Short type) {
        if(materials==null||materials.size()==0){
            return;
        }
        //获取图片素材
        List<WmMaterial> dbMaterials = wmMaterialMapper.selectList(new LambdaQueryWrapper<WmMaterial>()
                .in(WmMaterial::getUrl, materials));
        if(dbMaterials==null||dbMaterials.size()==0||dbMaterials.size()!=materials.size()){
            //素材失效
            throw new CustomException(AppHttpCodeEnum.MATERIASL_REFERENCE_FAIL);
        }
        //获取图片id列表
        List<Integer> idList = dbMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
        //批量插入
        wmNewsMaterialMapper.saveRelations(idList,newsId,type);
    }

    /**
     * 查询文章详情
     * @param id 文章id
     * @return
     */
    @Override
    public ResponseResult getNews(Integer id) {
        //1、检查参数
        if(id==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、查询文章数据
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }else{
            return ResponseResult.okResult(wmNews);
        }
    }

    /**
     * 删除文章
     * @param id 文章id
     * @return
     */
    @Override
    @Transactional
    public ResponseResult delNews(Integer id) {
        //1、检查参数
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章id不可缺少");
        }
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //2、已发布的文章不能删除
        if(wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章已发布，不能删除");
        }
        //3、删除文章表数据
        wmNewsMapper.deleteById(id);
        //4、删除文章图片关联表数据
        wmNewsMaterialMapper.delete(new LambdaQueryWrapper<WmNewsMaterial>()
                .eq(WmNewsMaterial::getNewsId,id));
        return ResponseResult.okResult(null);
    }

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    @Override
    public ResponseResult down_or_up(WmNewsDto dto) {
        //1、检查参数
        if(dto==null||dto.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"文章id不可缺少");
        }
        Integer newsId = dto.getId();
        WmNews wmNews = wmNewsMapper.selectById(newsId);
        if(wmNews==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //2、只有已发布的文章可以上下架
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"只有已发布的文章可以上下架");
        }
        //3、修改文章上下架状态
        wmNews.setEnable(dto.getEnable());
        int i = wmNewsMapper.updateById(wmNews);
        if(i<=0){
            log.error("修改文章上下架状态失败,dto:{}",dto);
        }
        return ResponseResult.okResult(null);
    }
}
