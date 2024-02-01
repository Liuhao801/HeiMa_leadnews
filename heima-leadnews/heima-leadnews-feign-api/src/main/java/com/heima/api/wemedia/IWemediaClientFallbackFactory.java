package com.heima.api.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmUser;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IWemediaClientFallbackFactory implements FallbackFactory<IWemediaClient> {
    @Override
    public IWemediaClient create(Throwable throwable) {
        return new IWemediaClient() {
            @Override
            public WmUser findWmUserByName(String name) {
                log.info("触发降级逻辑，熔断异常");
                return null;
            }

            @Override
            public ResponseResult saveWmUser(WmUser wmUser) {
                log.info("触发降级逻辑，熔断异常");
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }
        };
    }
}
