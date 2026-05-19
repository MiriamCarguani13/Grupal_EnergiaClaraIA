package com.energiaclara.infrastructure.security.context;

import java.util.UUID;

public final class TenantContext {
    private static final ThreadLocal<UUID> HOLDER = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId) {
        HOLDER.set(tenantId);
    }

    public static UUID get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
