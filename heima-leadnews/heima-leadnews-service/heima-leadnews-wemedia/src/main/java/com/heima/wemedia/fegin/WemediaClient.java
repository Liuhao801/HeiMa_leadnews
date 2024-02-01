package com.heima.wemedia.fegin;

import com.heima.api.wemedia.IWemediaClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class WemediaClient implements IWemediaClient {

    @Autowired
    private WmUserService wmUserService;

    /**
     * 根据用户名获取用户信息
     * @param name
     * @return
     */
    @GetMapping("/api/v1/user/findByName/{name}")
    @Override
    public WmUser findWmUserByName(@PathVariable String name) {
        log.info("根据用户名获取用户信息,name:{}",name);
        return wmUserService.findWmUserByName(name);
    }

    /**
     * 保存自媒体端用户
     * @param wmUser
     * @return
     */
    @PostMapping("/api/v1/wm_user/save")
    @Override
    public ResponseResult saveWmUser(@RequestBody WmUser wmUser) {
        log.info("保存自媒体端用户,wmuser:{}",wmUser);
        return wmUserService.saveWmUser(wmUser);
    }
}
