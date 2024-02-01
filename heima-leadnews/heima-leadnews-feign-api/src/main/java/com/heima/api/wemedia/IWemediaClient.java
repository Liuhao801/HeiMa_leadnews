package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "leadnews-wemedia",fallbackFactory = IWemediaClientFallbackFactory.class)
public interface IWemediaClient {

    /**
     * 根据用户名获取用户信息
     * @param name
     * @return
     */
    @GetMapping("/api/v1/user/findByName/{name}")
    WmUser findWmUserByName(@PathVariable("name") String name);

    /**
     * 保存自媒体端用户
     * @param wmUser
     * @return
     */
    @PostMapping("/api/v1/wm_user/save")
    ResponseResult saveWmUser(@RequestBody WmUser wmUser);

}