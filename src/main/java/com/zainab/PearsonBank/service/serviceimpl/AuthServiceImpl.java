package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.EmailOtp;
import com.zainab.PearsonBank.entity.User;
import com.zainab.PearsonBank.entity.UserSession;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.EmailOtpRepository;
import com.zainab.PearsonBank.repository.SessionRepository;
import com.zainab.PearsonBank.repository.UserRepository;
import com.zainab.PearsonBank.security.CustomUserDetails;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final EmailOtpRepository otpRepository;
    private final UserRepository userRepository;
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
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getAppPassword())) {
            throw new RuntimeException("Invalid Password!");
        }

        if (!user.isProfileEnabled()) {
            throw new RuntimeException("User profile is disabled!");
        }

        if (user.isDefaultPassword() && user.getDefaultPasswordIssuedAt()
                .isBefore(LocalDateTime.now().minusHours(24))) {
            throw new RuntimeException("Default password expired. Please reset your password!");
        }

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String token = jwtTokenProvider.generateAccessToken(customUserDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        LocalDateTime now = LocalDateTime.now();
        boolean isFirstTimeLogin = user.isDefaultPassword() && user.getLastLoginDate() == null;

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(now.plusDays(7));
        user.setLastLoginDate(now);
        userRepository.save(user);

        UserSession session = new UserSession();
        session.setUserId(user.getId());
        session.setAccessToken(token);
        session.setRefreshToken(refreshToken);
        session.setLastActivity(now);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusMinutes(20));
        session.setIpAddress(loginRequest.getIpAddress());
        session.setUserAgent(loginRequest.getUserAgent());
        sessionRepository.save(session);

        return new JwtResponse(String.valueOf(user.getId()), user.getEmail(), user.getRole(),
                token, refreshToken, "Bearer ", now, isFirstTimeLogin);
    }

    @Transactional
    @Override
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with this email: " + forgotPasswordRequest.getEmail()));

        try {
            String resetToken = generateResetToken();
            user.setResetPasswordToken(resetToken);
            user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(24));
            userRepository.save(user);

            String resetUrl = appBaseUrl + "api/validate-reset-token?token=" + resetToken; // this should be frontend base url

            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setSubject(EmailUtils.PASSWORD_RESET_EMAIL_SUBJECT.getTemplate());
            emailDetails.setBody(EmailUtils.PASSWORD_RESET_EMAIL_BODY.format(user.getFirstName(), resetUrl));
            emailDetails.setRecipient(user.getEmail());

            eventPublisher.publishEvent(new EmailEvent(emailDetails));
        } catch (Exception e) {
            log.error("Failed to send reset password email - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public TokenValidationResponse validateResetToken(String token) {
        try {
            User user = userRepository.findByResetPasswordToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid reset token"));

            if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired");
            }

            user.setResetPasswordTokenVerified(true);
            userRepository.save(user);

            return new TokenValidationResponse("00", "Token Is Valid", true, user.getEmail());
        } catch (Exception e) {
            return new TokenValidationResponse("40", e.getMessage(), false, null);
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        User user = userRepository.findByResetPasswordToken(resetPasswordRequest.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid Reset Token!"));

        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset Token has expired!");
        }

        if (!user.isResetPasswordTokenVerified()) {
            throw new RuntimeException("Reset Password Token Not Validated!");
        }

        String hashedPassword = passwordEncoder.encode(resetPasswordRequest.getNewPassword());
        if (user.getAppPassword() != null && user.getAppPassword().equals(hashedPassword)) {
            throw new RuntimeException("You cannot use old password!");
        }

        user.setAppPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        user.setResetPasswordTokenVerified(false);
        userRepository.save(user);
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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token mismatch!");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(new CustomUserDetails(user));
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();

        UserSession session = new UserSession();
        session.setUserId(user.getId());
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
                "role", user.getRole().name()
        ));
    }

    @Override
    public void sendEmailOtp(String name, String email, String type) {
//        otpRepository.findByEmail(email)
//                .ifPresent(otpRepository::delete);
        otpRepository.deleteByEmail(email);
        otpRepository.flush();

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
        Optional<User> oCustomer = userRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) {
            log.info("No user found for the id passed!!");
            throw new RuntimeException("User Not Found!");
        }

        User user = oCustomer.get();
        String hashedPassword = passwordEncoder.encode(password);

        user.setAppPassword(hashedPassword);
        user.setDefaultPassword(false);
        userRepository.save(user);

        return "Password set successfully!";
    }

    @Override
    public String changeAppPassword(String customerId, String oldPassword, String newPassword) {
        Optional<User> oCustomer = userRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) {
            log.info("No user found for the id passed!");
            throw new RuntimeException("User Not Found!");
        }

        User user = oCustomer.get();
        String hashedPassword = passwordEncoder.encode(newPassword);

        if (!passwordEncoder.matches(oldPassword, user.getAppPassword())) {
            log.info("Incorrect Password!");
            throw new RuntimeException("Incorrect Password!!");
        }

        if (user.isDefaultPassword()) {
            log.info("User should use Set Password option to change default password!");
            throw new RuntimeException("Use the set password option to change your default password!!");
        }

        if(user.getAppPassword() != null && user.getAppPassword().equals(hashedPassword)) {
            log.info("User cannot reuse their old password!");
            throw new RuntimeException("You cannot reuse your old password!!");
        }

        user.setAppPassword(hashedPassword);
        userRepository.save(user);

        return "Password changed successfully!";
    }

    @Override
    public String setTransactionPin(String customerId, String transactionPin) {
        Optional<User> oCustomer = userRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) return "User Not Found";

        User user = oCustomer.get();
        String hashedPin = passwordEncoder.encode(transactionPin);

        user.setTransactionPin(hashedPin);
        userRepository.save(user);

        return "Pin set successfully!";
    }

    @Override
    public String changeTransactionPin(String customerId, String oldTransactionPin, String newTransactionPin) {
        Optional<User> oCustomer = userRepository.findById(UUID.fromString(customerId));
        if (oCustomer.isEmpty()) {
            log.error("No user found for the id passed!!");
            throw new RuntimeException("User Not Found!");
        }

        User user = oCustomer.get();
        String hashedPin = passwordEncoder.encode(newTransactionPin);

        if (!passwordEncoder.matches(oldTransactionPin, user.getTransactionPin())) {
            log.error("Incorrect Pin!");
            throw new RuntimeException("Incorrect Pin!!");
        }

        if(user.getTransactionPin() != null && user.getTransactionPin().equals(hashedPin)) {
            log.info("User cannot reuse their old pin!");
            throw new RuntimeException("You cannot reuse your old pin!!");
        }

        user.setTransactionPin(hashedPin);
        userRepository.save(user);

        return "Pin changed successfully!";
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

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);

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

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found!"));

            sessionRepository.revokeAllSessionsByUserId(user.getId());
            user.setRefreshToken(null);
            user.setRefreshTokenExpiry(null);
            userRepository.save(user);

            // Add a token version that gets incremented on password changes or full logout
            //        user.setTokenVersion(user.getTokenVersion() + 1);
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
