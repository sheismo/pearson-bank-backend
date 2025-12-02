package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.entity.EmailOtp;
import com.zainab.PearsonBank.entity.UserSession;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.repository.EmailOtpRepository;
import com.zainab.PearsonBank.repository.SessionRepository;
import com.zainab.PearsonBank.security.JwtTokenProvider;
import com.zainab.PearsonBank.service.AuthService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.EmailUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final EmailOtpRepository otpRepository;
    private final CustomerRepository customerRepository;
    private final SessionRepository sessionRepository;
    private final EmailService emailService;
    private final AccountHelper accountHelper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.name}")
    private String appName;

    @Value("${app.supportMail}")
    private String appSupportMail;

    @Value("${app.baseUrl}")
    private String appBaseUrl;

    @Override
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Customer not found!"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), customer.getAppPassword())) {
            throw new RuntimeException("Invalid Password!");
        }

        if (!customer.isProfileEnabled()) {
            throw new RuntimeException("Customer profile is disabled!");
        }

        String token = jwtTokenProvider.generateAccessToken(
                new User(customer.getEmail(), customer.getAppPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(customer.getRole().name())))
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getEmail());
        customer.setRefreshToken(refreshToken);
        customer.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        customerRepository.save(customer);

        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();
        session.setUserId(customer.getId());
        session.setAccessToken(token);
        session.setRefreshToken(refreshToken);
        session.setLastActivity(now);
        session.setCreatedAt(now);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        sessionRepository.save(session);

        return new JwtResponse(String.valueOf(customer.getId()), customer.getEmail(), customer.getRole(),  token, refreshToken, "Bearer ");
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        Customer customer = customerRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Customer not found with this email: " + forgotPasswordRequest.getEmail()));

        try {
            String resetToken = generateResetToken();
            customer.setResetPasswordToken(resetToken);
            customer.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
            customerRepository.save(customer);

            String resetUrl = appBaseUrl + "/reset-password?token=" + resetToken;

            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setSubject(EmailUtils.PASSWORD_RESET_EMAIL_SUBJECT.getTemplate());
            emailDetails.setBody(EmailUtils.PASSWORD_RESET_EMAIL_BODY.format(customer.getFirstName(), resetUrl));
            emailDetails.setRecipient(customer.getEmail());

            eventPublisher.publishEvent(new EmailEvent(emailDetails));
        } catch (Exception e) {
            log.error("Failed to send reset password email - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public TokenValidationResponse validateResetToken(String token) {
        try {
            Customer customer = customerRepository.findByResetPasswordToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid reset token"));

            if (customer.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired");
            }

            return new TokenValidationResponse("00", "Token Is Valid", true, customer.getEmail());
        } catch (Exception e) {
            return new TokenValidationResponse("40", e.getMessage(), false, null);
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        Customer customer = customerRepository.findByResetPasswordToken(resetPasswordRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid Reset Token!"));

        if (customer.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset Token has expired!");
        }

        String hashedPassword = passwordEncoder.encode(resetPasswordRequest.getNewPassword());
        if (customer.getAppPassword() != null && customer.getAppPassword().equals(hashedPassword)) {
            throw new RuntimeException("You cannot use old password!");
        }

        customer.setAppPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        customer.setResetPasswordToken(null);
        customer.setResetPasswordTokenExpiry(null);
        customerRepository.save(customer);
    }

    @Override
    public ResponseEntity<?> generateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token!");
        }

        String email = jwtTokenProvider.extractUsername(refreshToken);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!refreshToken.equals(customer.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token mismatch!");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        customer.setRefreshToken(refreshToken);
        customer.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        customerRepository.save(customer);

        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();
        session.setUserId(customer.getId());
        session.setAccessToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setLastActivity(now);
        session.setCreatedAt(now);
        session.setExpiresAt(LocalDateTime.now().plusMinutes(20));
        sessionRepository.save(session);

        return ResponseEntity.ok(Map.of(
                "token", newAccessToken,
                "type", "Bearer",
                "email", email,
                "role", customer.getRole().name()
        ));
    }

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
        if (type.equals("Email Verification")) {
            subject = EmailUtils.OTP_VERIFICATION_EMAIL_SUBJECT.getTemplate();
            body = EmailUtils.OTP_VERIFICATION_EMAIL_BODY.format(name, appName, otp);
        } else {
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

        customer.setAppPassword(hashedPassword);
        customer.setProfileEnabled(true);
        customerRepository.save(customer);
        return "Password set successfully!";
    }

    @Override
    public String changeAppPassword(String customerId, String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            return "Failed: Old Password cannot be the same as New Password";
        }

        if (confirmAppPassword(customerId, oldPassword)) { //&& accountHelper.isValidPassword(customerId, newPassword)
            // save to cred history table
            return setAppPassword(customerId, newPassword);
        }
        return "Error occurred: Could not change user app assword!";
    }

    @Override
    public boolean confirmAppPassword(String customerId, String password) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return false;

        Customer customer = oCustomer.get();
        return passwordEncoder.matches(password, customer.getAppPassword());
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

        if (confirmTransactionPin(customerId, oldTransactionPin)) { // && accountHelper.isValidPin(customerId, newTransactionPin)
            // save to cred history table
            return setTransactionPin(customerId, newTransactionPin);
        }

        return "Failed to set transaction pin!";
    }

    @Override
    public boolean confirmTransactionPin(String customerId, String transactionPin) {
        Optional<Customer> oCustomer = customerRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return false;

        Customer customer = oCustomer.get();
        return passwordEncoder.matches(transactionPin, customer.getTransactionPin());
    }

    @Override
    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.FAILED.getCode(), AccountResponses.FAILED.getMessage(),
                        "Invalid Request"));
            }
            String email = jwtTokenProvider.extractUsername(token);

            UserSession session = sessionRepository.findByAccessToken(token);
            if (session != null) {
                session.setRevoked(true);
                sessionRepository.save(session);
            }

            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));

            customer.setRefreshToken(null);
            customer.setRefreshTokenExpiry(null);
            customerRepository.save(customer);

            // You might want to add the access token to a blacklist here
            // For a more secure implementation

            return ResponseEntity.ok(new AppResponse<>(AccountResponses.SUCCESS.getCode(), "Logged out successfully!",
                            null));
        } catch (Exception e) {
            log.error("Logout processing failed ", e);
            return ResponseEntity.badRequest().body(
                    new AppResponse<>(AccountResponses.FAILED.getCode(), "Failed to process logout request!",
                            null));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> logoutAllDevices(HttpServletRequest request) {
        try {
            String token = extractTokenFromRequest(request);
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.FAILED.getCode(), AccountResponses.FAILED.getMessage(),
                        "Invalid Request!"));
            }
            String email = jwtTokenProvider.extractUsername(token);

            UserSession session = sessionRepository.findByAccessToken(token);
            if (session != null) {
                session.setRevoked(true);
                sessionRepository.save(session);
            }

            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Customer not found!"));

            sessionRepository.revokeAllSessionsByUserId(customer.getId());
            customer.setRefreshToken(null);
            customer.setRefreshTokenExpiry(null);
            customerRepository.save(customer);

            // Add a token version that gets incremented on password changes or full logout
            //        customer.setTokenVersion(customer.getTokenVersion() + 1);
            return ResponseEntity.ok(new AppResponse<>(AccountResponses.SUCCESS.getCode(), "Logged out of all devices successfully!",
                    null));

        } catch (Exception e) {
            log.error("Logout processing failed ", e);
            return ResponseEntity.badRequest().body(
                    new AppResponse<>(AccountResponses.FAILED.getCode(), "Failed to process logout all devices request!",
                            null));
        }
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
