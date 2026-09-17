package com.aft.api.user.service;

import java.util.Locale;
import com.aft.api.common.exception.ValidationException;
import com.aft.api.user.entity.PasswordHistory;
import com.aft.api.user.repository.PasswordHistoryRepository;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.data.domain.Limit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PasswordPolicyService {
    public static final int MIN_LENGTH = 12;
    public static final int HISTORY_DEPTH = 5;

    private static final Pattern UPPER = Pattern.compile("[A-ZÇĞİÖŞÜ]");
    private static final Pattern LOWER = Pattern.compile("[a-zçğıöşü]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SYMBOL = Pattern.compile("[^\\p{L}\\d]");

    private final PasswordHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordPolicyService(PasswordHistoryRepository historyRepository, PasswordEncoder passwordEncoder) {
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public void validateFormat(String rawPassword, String... identifiers) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new ValidationException("Parola en az " + MIN_LENGTH + " karakter olmalıdır.");
        }
        if (!UPPER.matcher(rawPassword).find() || !LOWER.matcher(rawPassword).find() || !DIGIT.matcher(rawPassword).find() || !SYMBOL.matcher(rawPassword).find()) {
            throw new ValidationException("Parola büyük harf, küçük harf, rakam ve simge içermelidir.");
        }
        String lowered = rawPassword.toLowerCase(Locale.ROOT);
        for (String identifier : identifiers) {
            String localPart = identifier == null ? "" : identifier.split("@")[0];
            if (!localPart.isBlank() && lowered.contains(localPart.toLowerCase(Locale.ROOT))) {
                throw new ValidationException("Parola kullanıcı adını veya e-posta adresini içermemelidir.");
            }
        }
    }

    @Transactional(readOnly = true)
    public void validateChange(UUID userId, String rawPassword, String... identifiers) {
        validateFormat(rawPassword, identifiers);
        List<PasswordHistory> recent =
                historyRepository.findByUserIdOrderByCreatedAtDesc(userId, Limit.of(HISTORY_DEPTH));
        boolean reused = recent.stream()
                .anyMatch(entry -> passwordEncoder.matches(rawPassword, entry.getPasswordHash()));
        if (reused) {
            throw new ValidationException("Son " + HISTORY_DEPTH + " parola tekrar kullanılamaz.");
        }
    }

    @Transactional
    public void remember(UUID userId, String passwordHash) {
        historyRepository.save(new PasswordHistory(userId, passwordHash));
        historyRepository.deleteOlderThan(userId, HISTORY_DEPTH);
    }

}
