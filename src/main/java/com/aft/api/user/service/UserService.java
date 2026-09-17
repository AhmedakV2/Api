package com.aft.api.user.service;

import java.util.Locale;
import com.aft.api.common.exception.ConflictException;
import com.aft.api.common.exception.NotFoundException;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.user.dto.CreateUserRequest;
import com.aft.api.user.dto.UpdateUserRequest;
import com.aft.api.user.dto.UserDto;
import com.aft.api.user.entity.Role;
import com.aft.api.user.entity.RoleCode;
import com.aft.api.user.entity.UserAccount;
import com.aft.api.user.entity.UserStatus;
import com.aft.api.user.mapper.UserMapper;
import com.aft.api.user.repository.RoleRepository;
import com.aft.api.user.repository.UserRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordPolicyService passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordPolicyService passwordPolicy,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }
    @Transactional
    public UserDto create(CreateUserRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ConflictException("Bu kullanıcı adı zaten kayıtlı: " + username);
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Bu e-posta zaten kayıtlı: " + email);
        }
        passwordPolicy.validateFormat(request.password(), username, email);

        String hash = passwordEncoder.encode(request.password());
        UserAccount user = new UserAccount(username, email, hash, request.displayName().trim(),
                request.localeOrDefault());
        request.roles().forEach(code -> user.grant(requireRole(code)));
        UserAccount saved = userRepository.save(user);
        passwordPolicy.remember(saved.getId(), hash);
        log.info("Kullanıcı oluşturuldu id={}", saved.getId());
        return userMapper.toDto(saved);
    }
    @Transactional(readOnly = true)
    public Page<UserDto> search(String query, UserStatus status, Pageable pageable) {
        String normalized = (query == null || query.isBlank()) ? null : query.trim();
        return userRepository.search(normalized, status, pageable).map(userMapper::toDto);
    }
    @Transactional(readOnly = true)
    public UserDto get(UUID id) {
        return userMapper.toDto(requireUser(id));
    }
    @Transactional
    public UserDto update(UUID id, UpdateUserRequest request) {
        UserAccount user = requireUser(id);
        if (request.displayName() != null && !request.displayName().isBlank()) {
            user.rename(request.displayName().trim());
        }
        if (request.locale() != null) {
            user.changeLocale(request.locale());
        }
        if (request.status() != null) {
            user.changeStatus(request.status());
        }
        if (request.mfaEnabled() != null) {
            user.setMfaEnabled(request.mfaEnabled());
        }
        return userMapper.toDto(user);
    }
    @Transactional
    public void disable(UUID id) {
        UserAccount user = requireUser(id);
        if (user.getStatus() == UserStatus.DISABLED) {
            return;
        }
        user.changeStatus(UserStatus.DISABLED);
        log.info("Kullanıcı pasifleştirildi id ={}", id);
    }

    @Transactional
    public UserDto assignRole(UUID id, RoleCode code) {
        UserAccount user = requireUser(id);
        user.grant(requireRole(code));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto revokeRole(UUID id, RoleCode code) {
        UserAccount user = requireUser(id);
        if (!user.revoke(requireRole(code))) {
            throw new ValidationException("Kullanıcının son rolü kaldırılamaz");
        }
        return userMapper.toDto(user);
    }

    @Transactional
    public void changePassword(UUID id, String currentPassword, String newPassword) {
        UserAccount user = requireUser(id);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("Mevcut parola doğrulanamadı");
        }
        passwordPolicy.validateChange(id, newPassword, user.getUsername(), user.getEmail());

        String hash = passwordEncoder.encode(newPassword);
        user.changePasswordHash(hash);
        passwordPolicy.remember(id, hash);
        log.info("Parola değiştirildi id={}", id);
    }

    private UserAccount requireUser(UUID id) {
        return userRepository.findWithRolesById(id)
                .orElseThrow(() -> NotFoundException.of("UserAccount", id));
    }

    private Role requireRole(RoleCode code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> NotFoundException.of("Role", code));
    }
}
