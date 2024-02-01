package com.heima.admin.interceptor;

import com.heima.model.admin.pojos.AdUser;
import com.heima.utils.thread.AdminThreadLocalUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AdminTokenInterceptor implements HandlerInterceptor {
    /**
     * 将用户id存入threadlocal中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取header信息
        String userId = request.getHeader("userId");
        if(userId!=null){
            AdUser adUser = new AdUser();
            adUser.setId(Integer.valueOf(userId));
            AdminThreadLocalUtils.setUser(adUser);
        }
        return true;
    }

    /**
     * 清理threadlocal
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AdminThreadLocalUtils.clear();
    }
}
