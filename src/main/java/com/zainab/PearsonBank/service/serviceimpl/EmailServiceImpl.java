package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.EmailDetails;
import com.zainab.PearsonBank.entity.FailedEmail;
import com.zainab.PearsonBank.repository.FailedEmailRepository;
import com.zainab.PearsonBank.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    private FailedEmailRepository failedEmailRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${appMailName}")
    private String senderName;

    @Async
    @Retryable(
            retryFor = { MailException.class , MessagingException.class },
            maxAttempts = 3, backoff = @Backoff(delay = 180000)
    )
    @Override
    public void sendEmailAlert(EmailDetails emailDetails) {
        log.info("Sending email to {}", emailDetails.getRecipient());
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            helper.setFrom(senderEmail, senderName);
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getBody(), true);

            javaMailSender.send(message);
            log.info("Mail sent successfully::");
        }  catch (UnsupportedEncodingException e) {
            log.error("Failed to encode sender name '{}'. Email not sent to {}", senderName, emailDetails.getRecipient(), e);
//            throw new RuntimeException(e);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}: {}", emailDetails.getRecipient(), e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Recover
    public void recover(MailException e, EmailDetails emailDetails) {
        log.error("All retries failed for email to {}. Saving to manual retry queue", emailDetails.getRecipient(), e);

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
}
