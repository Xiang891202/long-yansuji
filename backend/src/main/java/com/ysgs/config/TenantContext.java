package com.ysgs.config;

public class TenantContext {
    private static final ThreadLocal<Integer> currentTenant = new ThreadLocal<>();

    public static void setTenantId(Integer id) {
        currentTenant.set(id);
    }

    public static Integer getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}