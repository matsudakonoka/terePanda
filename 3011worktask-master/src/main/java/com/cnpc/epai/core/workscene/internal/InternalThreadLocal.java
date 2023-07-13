package com.cnpc.epai.core.workscene.internal;

public final class InternalThreadLocal {
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    public static Object get() {
        return THREAD_LOCAL.get();
    }

    public static void set(Object value) {
        THREAD_LOCAL.set(value);
    }
}
