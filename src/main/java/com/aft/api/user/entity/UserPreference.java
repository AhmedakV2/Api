package com.aft.api.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_preference")
public class UserPreference {
    @EmbeddedId
    private Key key;

    @Column(name = "pref_value", nullable = false, length = 512)
    private String value;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
    protected UserPreference() {}
    public UserPreference(UUID userId, String prefKey, String value) {
        this.key = new Key(userId, prefKey);
        this.value = value;
    }
    public void changeValue(String newValue) {
        this.value = newValue;
        this.updatedAt = Instant.now();
    }
    public Key getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    @Embeddable
    public record Key(@Column(name = "user_id") UUID userId,
                      @Column(name = "pref_key", length= 64) String prefKey) implements Serializable {
        public Key {
            Objects.requireNonNull(userId);
            Objects.requireNonNull(prefKey);
        }
    }
}
