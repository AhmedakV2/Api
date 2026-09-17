package com.aft.api.security.repository;

import com.aft.api.security.entity.ApiKey;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findByOrgIdOrderByCreatedAtDesc(UUID orgId);

    List<ApiKey> findByOrgIdAndOwnerIdAndNameAndRevokedAtIsNull(UUID orgId, UUID ownerId, String name);

    @Modifying
    @Query("UPDATE ApiKey k SET k.lastUsedAt = :now WHERE k.id = :id")
    void touch(@Param("id") UUID id, @Param("now") Instant now);
}
