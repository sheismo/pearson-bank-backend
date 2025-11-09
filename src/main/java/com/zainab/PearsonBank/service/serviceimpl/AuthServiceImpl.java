package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.EmailDetails;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.entity.EmailOtp;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.repository.EmailOtpRepository;
import com.zainab.PearsonBank.service.AuthService;
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
public class AuthServiceImpl implements AuthService {
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

    @Override
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

    @Override
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
    public String setAppPassword(String customerId, String password) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return "Customer Not Found";

        Customer customer = oCustomer.get();
        String hashedPassword = passwordEncoder.encode(password);
        if(customer.getAppPassword() != null && customer.getAppPassword().equals(hashedPassword)) return "You cannot use your old password";

        customer.setTransactionPin(hashedPassword);
        customerRepository.save(customer);
        return "Password set successfully!";
    }

    @Override
    public String changeAppPassword(String customerId, String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            return "Failed: Old Password cannot be the same as New Password";
        }

        if (confirmAppPassword(customerId, oldPassword)) { //&& accountHelper.canUsePassword(customerId, newPassword)
            // save to cred history table
            return setAppPassword(customerId, newPassword);
        }

        return "Failed!";
    }

    @Override
    public boolean confirmAppPassword(String customerId, String password) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return false;

        Customer customer = oCustomer.get();
        return passwordEncoder.matches(password, customer.getTransactionPin());
    }

    @Override
    public String setTransactionPin(String customerId, String transactionPin) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return "Customer Not Found";

        Customer customer = oCustomer.get();
        String hashedPin = passwordEncoder.encode(transactionPin);
        if(customer.getTransactionPin() != null && customer.getTransactionPin().equals(hashedPin)) return "You cannot use your old pin";

        customer.setTransactionPin(hashedPin);
        customerRepository.save(customer);
        return "Pin set successfully!";
    }

    @Override
    public String changeTransactionPin(String customerId, String oldTransactionPin, String newTransactionPin) {
        if (oldTransactionPin.equals(newTransactionPin)) {
            return "Failed: Old Pin cannot be the same as New Pin";
        }

        if (confirmTransactionPin(customerId, oldTransactionPin)) { // && accountHelper.canUsePin(customerId, newTransactionPin)
            // save to cred history table
            return setAppPassword(customerId, newTransactionPin);
        }

        return "Failed!";
    }

    @Override
    public boolean confirmTransactionPin(String customerId, String transactionPin) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return false;

        Customer customer = oCustomer.get();
        return passwordEncoder.matches(transactionPin, customer.getTransactionPin());
    }

}
