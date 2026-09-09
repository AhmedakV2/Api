package com.aft.api.user.repository;

import com.aft.api.user.entity.UserPreference;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreference.Key> {

    List<UserPreference> findByKeyUserId(UUID userId);

    void deleteByKeyUserId(UUID userId);
}
