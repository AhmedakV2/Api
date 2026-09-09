package com.aft.api.user.repository;

import com.aft.api.user.entity.PasswordHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, UUID> {

    List<PasswordHistory> findByUserIdOrderByCreatedAtDesc(UUID userId, Limit limit);

    @Modifying
    @Query("""
            DELETE FROM PasswordHistory p
            WHERE p.userId = :userId
              AND p.id NOT IN (
                  SELECT h.id FROM PasswordHistory h
                  WHERE h.userId = :userId
                  ORDER BY h.createdAt DESC
                  LIMIT :keep)
            """)
    void deleteOlderThan(@Param("userId") UUID userId, @Param("keep") int keep);
}
