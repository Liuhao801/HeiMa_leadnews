package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.api.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.exception.CustomException;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.*;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmSensitiveMapper wmSensitiveMapper;
    @Autowired
    private Tess4jClient tess4jClient;

    /**
     * 自媒体文章审核
     * @param id 自媒体文章id
     */
    @Override
    @Transactional
    @Async  //标明当前方法是一个异步方法
    public void autoScanWmNews(Integer id) {
        //1、查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if(wmNews==null){
            throw new RuntimeException("文章不存在");
        }
        //2、只有待审核的文章需要审核
        if(wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())){
            //3、获取文章的文本内容和图片信息
            Map<String,Object> textAndImages = handleTextAndImages(wmNews);

            //4、审核自管理的敏感词
            boolean isSensitive = handleSensitiveScan((String) textAndImages.get("content"), wmNews);
            if(!isSensitive) return;

//            //5、阿里云审核文本内容
//            boolean isTextScan = handleTextScan(wmNews, (String) textAndImages.get("content"));
//            //审核文本内容未通过
//            if(!isTextScan)return;
//            //6、阿里云审核图片
//            boolean isImagesScan = handleImagesScan(wmNews, (List<String>) textAndImages.get("images"));
//            //审核图片未通过
//            if(!isImagesScan)return;

            //7、审核通过，调用远程接口在app端保存或修改文章
            ResponseResult responseResult = saveAppArticle(wmNews);
            if(responseResult==null||!responseResult.getCode().equals(200)){
                throw new RuntimeException("WmNewsAutoScanServiceImpl-文章审核，保存app端相关文章数据失败");
            }
            //8、回填文章的articleId,并修改状态
            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews,WmNews.Status.PUBLISHED.getCode(),"审核成功");
        }
    }


    /**
     * 调用远程接口在app端保存或修改文章
     * @param wmNews
     * @return
     */
    @Override
    public ResponseResult saveAppArticle(WmNews wmNews) {
        //1、属性填充
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        //文章布局
        dto.setLayout(wmNews.getType());
        //频道
        WmChannel channel = wmChannelMapper.selectById(wmNews.getChannelId());
        if(channel!=null){
            dto.setChannelName(channel.getName());
        }
        //作者信息
        dto.setAuthorId(Long.valueOf(wmNews.getUserId()));
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if(wmUser!=null){
            dto.setAuthorName(wmUser.getName());
        }
        //设置文章id
        if(wmNews.getArticleId()!=null){
            dto.setId(wmNews.getArticleId());
        }
        dto.setCreatedTime(new Date());
        //2、远程调用IArticleClient接口
        ResponseResult responseResult = articleClient.saveArticle(dto);
        return responseResult;
    }

    /**
     * 获取文章的文本内容和图片信息
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        StringBuilder content=new StringBuilder();
        List<String> images=new ArrayList<>();

        //从内容中提取文本和图片
        if(StringUtils.isNotBlank(wmNews.getContent())){
            List<Map> maps = JSON.parseArray(wmNews.getContent(), Map.class);
            maps.forEach(map -> {
                if("text".equals(map.get("type"))){
                    content.append(map.get("value"));
                }
                if("image".equals(map.get("type"))){
                    images.add((String) map.get("value"));
                }
            });
        }
        //提取文章标题文本
        content.append("-"+wmNews.getTitle());
        //提取封面图片
        if(StringUtils.isNotBlank(wmNews.getImages())){
            String[] split = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(split));
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("content",content.toString());
        resultMap.put("images",images);
        return resultMap;
    }

    /**
     * 修改文章状态
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        int i = wmNewsMapper.updateById(wmNews);
        if(i<=0){
            log.info("修改文章状态失败，wmNews:{}",wmNews);
        }
    }

    /**
     * 审核自管理的敏感词
     * @param content
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(String content, WmNews wmNews) {
        boolean flag=true;
        //1、获取所有敏感词
        List<WmSensitive> wmSensitives = wmSensitiveMapper.selectList(null);
        List<String> sensitives = wmSensitives.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
        //2、初始化敏感词库
        SensitiveWordUtil.initMap(sensitives);
        //3、查询文章内容中是否存在敏感词
        Map<String, Integer> map = SensitiveWordUtil.matchWords(content);
        if(map.size()>0){
            updateWmNews(wmNews,WmNews.Status.FAIL.getCode(), "当前文章中存在违规内容"+map);
            flag=false;
        }
        return flag;
    }

    /**
     * 审核文本内容
     * @param wmNews
     * @param content
     * @return
     */
    private boolean handleTextScan(WmNews wmNews, String content) {
        boolean flag=true;
        if(StringUtils.isBlank(content)){
            return flag;
        }

        try {
            Map map = greenTextScan.greeTextScan(content);
            if(map!=null){
                //suggestion: pass block review
                //审核失败
                if("block".equals(map.get("suggestion"))){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.FAIL.getCode(),"当前文章中存在违规内容");
                }
                //不确定信息  需要人工审核
                if("review".equals(map.get("suggestion"))){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.ADMIN_AUTH.getCode(),"当前文章中存在存在不确定内容,需要人工审核");
                }
            }
        } catch (Exception e) {
            log.info("阿里云审核文本内容失败");
            flag=false;
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 审核图片
     * @param wmNews
     * @param images
     * @return
     */
    private boolean handleImagesScan(WmNews wmNews, List<String> images) {
        boolean flag=true;
        if(images==null || images.size()==0){
            return flag;
        }

        //从minio中下载图片
        //图片去重
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();
        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                //图片识别文字审核---begin-----
                //从byte[]转换为butteredImage
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage imageFile = ImageIO.read(in);
                //识别图片的文字
                String result = tess4jClient.doOCR(imageFile);
                //审核是否包含自管理的敏感词
                boolean isSensitive = handleSensitiveScan(result, wmNews);
                if (!isSensitive) {
                    return isSensitive;
                }
                //图片识别文字审核---end-----

                imageList.add(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Map map = greenImageScan.imageScan(imageList);
            if(map!=null){
                //suggestion: pass block review
                //审核失败
                if("block".equals(map.get("suggestion"))){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.FAIL.getCode(),"当前图片中存在违规内容");
                }
                //不确定信息  需要人工审核
                if("review".equals(map.get("suggestion"))){
                    flag=false;
                    updateWmNews(wmNews,WmNews.Status.ADMIN_AUTH.getCode(),"当前图片中存在存在不确定内容,需要人工审核");
                }
            }
        } catch (Exception e) {
            log.info("阿里云审核图片失败");
            flag=false;
            e.printStackTrace();
        }
        return flag;
    }

}
