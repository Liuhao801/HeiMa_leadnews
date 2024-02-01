package com.heima.utils.thread;

import com.heima.model.admin.pojos.AdUser;

public class AdminThreadLocalUtils {
    private final static ThreadLocal<AdUser> ADMIN_USER_THREAD_LOCAL=new ThreadLocal<>();

    /**
     * 添加用户
     * @param adUser
     */
    public static void setUser(AdUser adUser){
        ADMIN_USER_THREAD_LOCAL.set(adUser);
    }

    /**
     * 获取用户
     * @return
     */
    public static AdUser getUser(){
        return ADMIN_USER_THREAD_LOCAL.get();
    }

    /**
     * 清理用户
     */
    public static void clear(){
        ADMIN_USER_THREAD_LOCAL.remove();
    }
}
