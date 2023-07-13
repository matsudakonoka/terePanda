package com.cnpc.epai.core.worktask.util;

import java.util.Map;

/**
 * @ClassName: ThreadLocalUtil
 * @Description:
 * @Author
 * @Date 2022/10/28
 * @Version 1.0
 */
public class ThreadLocalUtil {
    private static final ThreadLocal<Map<String,Object>> threadLocal = new ThreadLocal<>();

    public static void map(Map<String,Object> map){
        threadLocal.set(map);
    }

    public static Object get(String key){
        Map<String,Object> map = threadLocal.get();
        return map == null?null:map.get(key);
    }

    public static void set(String key,Object Value){
        Map<String,Object> map = threadLocal.get();
        map.put(key,Value);
    }

    public static void clear(){
        threadLocal.remove();
    }
}
