package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.EmailDetails;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.entity.EmailOtp;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.repository.EmailOtpRepository;
import com.zainab.PearsonBank.service.CredentialService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {
    private final EmailOtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final AccountHelper accountHelper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.name}")
    private String appName;

    @Value("${app.supportMail}")
    private String appSupportMail;

    // Generate and send OTP
    public void sendEmailOtp(String name, String email, String type) {
        otpRepository.findByEmail(email)
                .ifPresent(otpRepository::delete);

        String otp = accountHelper.generateOTP();
        log.info("OTP is {} ", otp);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setEmail(email);
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));

        EmailDetails emailDetails = new EmailDetails();
        String subject = "";
        String body = "";
        switch (type) {
            case "Email Verification":
                subject = EmailUtils.OTP_VERIFICATION_EMAIL_SUBJECT.getTemplate();
                body = EmailUtils.OTP_VERIFICATION_EMAIL_BODY.format(name, appName, otp);
                break;
            default:
                subject = "New Email";
                body = "New Email Otp";
        }
        emailDetails.setSubject(subject);
        emailDetails.setBody(body);
        emailDetails.setRecipient(email);

        // save generated otp and send email
        try {
            otpRepository.save(emailOtp);
            eventPublisher.publishEvent(new EmailEvent(emailDetails));
        } catch (Exception e) {
            log.error("Failed to send otp - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Verify OTP
    public boolean verifyEmailOtp(String email, String otp) {
        log.info("OTP to be verified is {} ", otp);
        Optional<EmailOtp> validOtp = otpRepository.findTopByEmailAndOtpAndUsedFalseOrderByExpiryTimeDesc(email, otp);
        if (validOtp.isEmpty()) return false;

        EmailOtp emailOtp = validOtp.get();
        if (emailOtp.getExpiryTime().isBefore(LocalDateTime.now())) return false;

        // Mark OTP as used
        emailOtp.setUsed(true);
        otpRepository.save(emailOtp);

        return true;
    }

    @Override
    public String setTransactionPin(String customerId, String transactionPin) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return "1";

        Customer customer = oCustomer.get();
        String hashedPin = passwordEncoder.encode(transactionPin);
        if(customer.getTransactionPin() != null && customer.getTransactionPin().equals(hashedPin)) return "2";

        customer.setTransactionPin(hashedPin);
        customerRepository.save(customer);
        return "3";
    }

    @Override
    public boolean confirmTransactionPin(String customerId, String transactionPin) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return false;

        Customer customer = oCustomer.get();
        return passwordEncoder.matches(transactionPin, customer.getTransactionPin());
    }
}
