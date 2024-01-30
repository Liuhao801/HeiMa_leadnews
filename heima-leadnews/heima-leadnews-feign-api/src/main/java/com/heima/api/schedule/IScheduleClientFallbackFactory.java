package com.heima.api.schedule;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.dtos.Task;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IScheduleClientFallbackFactory implements FallbackFactory<IScheduleClient> {
    @Override
    public IScheduleClient create(Throwable throwable) {
        return new IScheduleClient() {
            @Override
            public ResponseResult addTask(Task task) {
                log.info("触发降级逻辑，熔断异常:{}",throwable);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            @Override
            public ResponseResult cancelTask(long taskId) {
                log.info("触发降级逻辑，熔断异常:{}",throwable);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }

            @Override
            public ResponseResult poll(int type, int priority) {
                log.info("触发降级逻辑，熔断异常:{}",throwable);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }
        };
    }
}
