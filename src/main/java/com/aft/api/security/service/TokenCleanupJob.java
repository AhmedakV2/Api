package com.aft.api.security.service;

import com.aft.api.security.repository.RefreshTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TokenCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(TokenCleanupJob.class);

    private final RefreshTokenRepository repository;

    public TokenCleanupJob(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void purgeExpired() {
        int removed = repository.deleteExpired(Instant.now().minus(7, ChronoUnit.DAYS));
        if (removed > 0) {
            log.info("Suresi dolmus yenileme jetonu silindi adet={}", removed);
        }
    }
}
