package com.zainab.PearsonBank.scheduler;

import com.zainab.PearsonBank.dto.EmailDetails;
import com.zainab.PearsonBank.entity.FailedEmail;
import com.zainab.PearsonBank.repository.FailedEmailRepository;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.types.EmailStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailedEmailRetryJob {

    private final FailedEmailRepository failedEmailRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelay = 1200000) // every 15 mins
    public void retryFailedEmails() {
        List<FailedEmail> failedEmails = failedEmailRepository.findAll();
        if (failedEmails.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed emails...", failedEmails.size());
        for (FailedEmail failed : failedEmails) {
            try {
                emailService.sendEmailAlert(new EmailDetails(
                        failed.getRecipient(),
                        failed.getSubject(),
                        failed.getBody(),
                        null
                ));
                failedEmailRepository.delete(failed);
                log.info("Resent email to {}", failed.getRecipient());
            } catch (Exception e) {
                log.error("Retry still failed for {}: {}", failed.getRecipient(), e.getMessage());

                if (failed.getRetryCount() >= failed.getMaxRetries()) {
                    failed.setStatus(EmailStatus.PERMANENTLY_FAILED);
                } else {
                    failed.setRetryCount(failed.getRetryCount() + 1);
                    failed.setLastTriedAt(LocalDateTime.now());
                    failed.setStatus(EmailStatus.FAILED);
                }
                failedEmailRepository.save(failed);
            }
        }
    }
}
