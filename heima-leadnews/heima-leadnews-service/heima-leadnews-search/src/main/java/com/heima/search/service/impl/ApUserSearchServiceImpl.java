package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.AppThreadLocalUtils;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户搜索历史记录
     * @param keyword
     * @param userId
     */
    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        //1、查询当前用户搜索记录
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);
        //2、若记录存在，将其更新为最近时间
        if(apUserSearch!=null){
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }
        //3、若记录不存在，新增记录
        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());

        //判断当前用户的搜索记录是否小于10条
        query = Query.query(Criteria.where("userId").is(userId));
        //按创建时间降序排列
        query.with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query, ApUserSearch.class);
        if(apUserSearches==null||apUserSearches.size()<10){
            //若小于10条，直接添加
            mongoTemplate.save(apUserSearch);
        }else{
            //否则，替换最后一条搜索记录
            ApUserSearch lastSearch = apUserSearches.get(apUserSearches.size() - 1);  //最后一条
            query=Query.query(Criteria.where("id").is(lastSearch.getId()));
            mongoTemplate.findAndReplace(query,apUserSearch);
        }
    }

    /**
     * 查询搜索历史
     * @return
     */
    @Override
    public ResponseResult findUserSearch() {
        //1、获取用户id
        ApUser apUser = AppThreadLocalUtils.getUser();
        if(apUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //2、根据用户id查询搜索记录，按创建时间降序
        Query query = Query.query(Criteria.where("userId").is(apUser.getId()))
                .with(Sort.by(Sort.Direction.DESC,"createdTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query, ApUserSearch.class);
        return ResponseResult.okResult(apUserSearches);
    }

    /**
     * 删除搜索历史
     * @param dto
     * @return
     */
    @Override
    public ResponseResult delUserSearch(HistorySearchDto dto) {
        //1、检查参数
        if(dto==null||dto.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2、获取当前用户
        ApUser apUser = AppThreadLocalUtils.getUser();
        if(apUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        //3、删除搜索记录
        mongoTemplate.remove(Query.query(Criteria.where("userId").is(apUser.getId())
                .and("id").is(dto.getId())), ApUserSearch.class);
        return ResponseResult.okResult(null);
    }
}
