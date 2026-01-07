package com.zainab.PearsonBank.utils;

import com.zainab.PearsonBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 * * * *") // runs every hour
    public void invalidateExpiredDefaultPasswords() {

        LocalDateTime expiryTime = LocalDateTime.now().minusHours(48);
        int updated = userRepository.invalidateExpiredDefaultPasswords(expiryTime);

        log.info("Invalidated {} expired default passwords", updated);
    }
}

