package com.aft.api.state.local;

record ExpiringEntry<T>(T value, long expiresAt) {
    boolean alive(long now) {
        return expiresAt <= 0 || expiresAt > now;
    }
}
