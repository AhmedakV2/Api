package com.aft.api.user.repository;

import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.entity.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserAccount, UUID> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserAccount> findWithRolesById(UUID id);

    @EntityGraph(attributePaths = "roles")
    Optional<UserAccount> findWithRolesByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "roles")
    Optional<UserAccount> findWithRolesByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "roles")
    @Query("""
            SELECT u FROM UserAccount u
            WHERE (:status IS NULL OR u.status = :status)
              AND (:q IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                              OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                              OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<UserAccount> search(@Param("q") String q, @Param("status") UserStatus status, Pageable pageable);
}
