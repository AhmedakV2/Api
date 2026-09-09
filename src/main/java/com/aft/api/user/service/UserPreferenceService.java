package com.aft.api.user.service;

import com.aft.api.common.exception.NotFoundException;
import com.aft.api.user.dto.PreferenceDto;
import com.aft.api.user.entity.UserPreference;
import com.aft.api.user.repository.UserPreferenceRepository;
import com.aft.api.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    public UserPreferenceService(UserPreferenceRepository preferenceRepository, UserRepository userRepository) {
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PreferenceDto> findAll(UUID userId) {
        requireUser(userId);
        return preferenceRepository.findByKeyUserId(userId).stream()
                .map(pref -> new PreferenceDto(pref.getKey().prefKey(), pref.getValue()))
                .toList();
    }

    @Transactional
    public List<PreferenceDto> replaceAll(UUID userId, List<PreferenceDto> incoming) {
        requireUser(userId);
        Map<String, UserPreference> existing = preferenceRepository.findByKeyUserId(userId).stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getKey().prefKey(), Function.identity()));

        for (PreferenceDto dto : incoming) {
            UserPreference current = existing.remove(dto.key());
            if (current == null) {
                preferenceRepository.save(new UserPreference(userId, dto.key(), dto.value()));
            } else {
                current.changeValue(dto.value());
            }
        }
        preferenceRepository.deleteAll(existing.values());
        return incoming;
    }

    private void requireUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw NotFoundException.of("UserAccount", userId);
        }
    }
}

