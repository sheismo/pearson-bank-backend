package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.EmailDetails;
import com.zainab.PearsonBank.entity.FailedEmail;
import com.zainab.PearsonBank.repository.FailedEmailRepository;
import com.zainab.PearsonBank.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class ApiEmailServiceImpl implements EmailService {
    @Value("${brevo.api.key}")
    private String apiKey;

    private final WebClient webClient;

    @Autowired
    private FailedEmailRepository failedEmailRepository;

    @Value("${app.generalMail}")
    private String senderEmail;

    @Value("${app.name}")
    private String senderName;

    @Async
    @Retryable(
            retryFor = { MailException.class , MessagingException.class },
            maxAttempts = 3, backoff = @Backoff(delay = 180000)
    )
    @Override
    public void sendEmailAlert(EmailDetails email) {
        try {
            log.info("Sending email via Brevo API to {}", email.getRecipient());

            webClient.post()
                    .uri("https://api.brevo.com/v3/smtp/email")
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(buildPayload(email))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Brevo email sent successfully");

        } catch (Exception e) {
            log.error("Brevo email failed", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> buildPayload(EmailDetails email) {
        return Map.of(
                "sender", Map.of(
                        "email", senderEmail,
                        "name", senderName
                ),
                "to", List.of(
                        Map.of("email", email.getRecipient())
                ),
                "subject", email.getSubject(),
                "htmlContent", email.getBody()
        );
    }

    @Recover
    public void recover(Exception e, EmailDetails emailDetails) {
        log.error("All retries failed for email to {}", emailDetails.getRecipient());

        FailedEmail failedEmail = FailedEmail.builder()
                .recipient(emailDetails.getRecipient())
                .subject(emailDetails.getSubject())
                .body(emailDetails.getBody())
                .failureReason(e.getMessage())
                .retryCount(3)
                .failedAt(LocalDateTime.now())
                .build();

        failedEmailRepository.save(failedEmail);
    }

    @Override
    public void sendEmailWithAttachment(EmailDetails emailDetails) throws MessagingException {

    }
}
