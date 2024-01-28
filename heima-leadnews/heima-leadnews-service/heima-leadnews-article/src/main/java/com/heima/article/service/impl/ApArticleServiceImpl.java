package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;

    private final static Short MAX_PAGE_SIZE=50;

    /**
     * 加载文章
     * @param dto
     * @param type  1为加载更多  2为加载最新
     * @return
     */
    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {
        //1、校验参数
        //分页参数校验
        Integer size = dto.getSize();
        if(size==null||size<=0){
            //默认查询10条数据
            size=10;
        }
        //最多查询50条数据
        size=Math.min(size,MAX_PAGE_SIZE);
        dto.setSize(size);

        //type参数校验
        if(!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE)&&!type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            type=ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //文章频道参数校验
        if(StringUtils.isBlank(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        //时间参数校验
        if(dto.getMaxBehotTime()==null)dto.setMaxBehotTime(new Date());
        if(dto.getMinBehotTime()==null)dto.setMinBehotTime(new Date());

        //2、查询数据
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, type);
        //3、封装结果并返回
        return ResponseResult.okResult(apArticles);
    }

    /**
     * 保存或修改文章信息到app端
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public ResponseResult saveArticle(ArticleDto dto) {

        //1、检查参数
        if(dto==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto,apArticle);
        if(apArticle.getId()==null){
            //2、若不存在id 保存文章表，文章配置表，文章内容表
            //2.1、保存文章表信息
            int insert = apArticleMapper.insert(apArticle);
            if(insert<=0){
                log.info("保存文章表信息失败,dto:{}",dto);
            }
            //2.2、保存文章配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            insert = apArticleConfigMapper.insert(apArticleConfig);
            if(insert<=0){
                log.info("保存文章配置失败,dto:{}",dto);
            }
            //2.3、保存文章内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            insert = apArticleContentMapper.insert(apArticleContent);
            if(insert<=0){
                log.info("保存文章内容失败,dto:{}",dto);
            }
        }else{
            //3、若存在id 修改文章表，文章内容表
            //3.1、修改文章表
            int i = apArticleMapper.updateById(apArticle);
            if(i<=0){
                log.info("修改文章表信息失败,dto:{}",dto);
            }
            //3.2、修改文章内容表
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(new LambdaQueryWrapper<ApArticleContent>()
                    .eq(ApArticleContent::getArticleId, apArticle.getId()));
            if(apArticleContent==null){
                apArticleContent=new ApArticleContent();
                apArticleContent.setArticleId(apArticle.getId());
                apArticleContent.setContent(dto.getContent());
                i = apArticleContentMapper.insert(apArticleContent);
                if(i<=0){
                    log.info("保存文章内容失败,dto:{}",dto);
                }
            }else{
                apArticleContent.setContent(dto.getContent());
                i = apArticleContentMapper.updateById(apArticleContent);
                if(i<=0){
                    log.info("修改文章内容失败,dto:{}",dto);
                }
            }

        }
        Long articleId = apArticle.getId();
        //4、异步调用 生成静态文件上传到minio中
        articleFreemarkerService.buildArticleToMinIO(articleId,dto.getContent());

        //5、返回文章表id
        return ResponseResult.okResult(articleId);
    }
}
