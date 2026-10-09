/*
 * Decompiled with CFR 0.152.
 */
package id.zailth.auth.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthSession {
    private final Map<UUID, Long> authenticatedPlayers = new ConcurrentHashMap<UUID, Long>();

    public void login(UUID uuid, long durationMillis) {
        long expiryTime = System.currentTimeMillis() + durationMillis;
        this.authenticatedPlayers.put(uuid, expiryTime);
    }

    public void login(UUID uuid) {
        this.authenticatedPlayers.put(uuid, Long.MAX_VALUE);
    }

    public void logout(UUID uuid) {
        this.authenticatedPlayers.remove(uuid);
    }

    public boolean isAuthenticated(UUID uuid) {
        Long expiryTime = this.authenticatedPlayers.get(uuid);
        if (expiryTime == null) {
            return false;
        }
        if (expiryTime == Long.MAX_VALUE) {
            return true;
        }
        if (System.currentTimeMillis() >= expiryTime) {
            this.authenticatedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    public long getRemainingTime(UUID uuid) {
        Long expiryTime = this.authenticatedPlayers.get(uuid);
        if (expiryTime == null) {
            return 0L;
        }
        if (expiryTime == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        long remaining = expiryTime - System.currentTimeMillis();
        if (remaining <= 0L) {
            this.authenticatedPlayers.remove(uuid);
            return 0L;
        }
        return remaining;
    }

    public void clear() {
        this.authenticatedPlayers.clear();
    }
}

