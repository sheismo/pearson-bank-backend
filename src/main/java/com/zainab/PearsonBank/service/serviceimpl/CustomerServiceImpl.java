package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.User;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.UserRepository;
import com.zainab.PearsonBank.service.AuthService;
import com.zainab.PearsonBank.service.CustomerService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.types.CurrencyType;
import com.zainab.PearsonBank.utils.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService  {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TransactionService transactionService;
    private final AuthService credentialService;
    private final AccountHelper accountHelper;
    private final MapperClass mapperClass;
    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.name}")
    private String appName;

    /**
     * To onboard a new user:
     * check if user has an existing account
     * save user details and do email verification
     * verify email and generate unique account no
     * send welcome mail to user email address
     */

    @Transactional
    @Override
    public AppResponse<?> onboardNewCustomer(CustomerRequest customerRequest) {
        log.info("Received request to onboard new user:::::");
        if (accountHelper.checkIfCustomerExistsByEmail(customerRequest.getEmail())) {
            if (accountHelper.checkIfCustomerIsVerified(customerRequest.getEmail())) {
                log.error("User already has an existing account registered to this email provided!");
                return AppResponse.builder()
                        .responseCode(AccountResponses.ACCOUNT_EXISTS.getCode())
                        .responseMessage(AccountResponses.ACCOUNT_EXISTS.getMessage())
                        .data(null)
                        .build();
            } else {
                log.error("User has already been onboarded, but email has not been verified!");
                return AppResponse.builder()
                        .responseCode(AccountResponses.ACCOUNT_EXISTS.getCode())
                        .responseMessage(AccountResponses.ACCOUNT_EXISTS.getMessage() + ": Email Address Not Verified!")
                        .data(null)
                        .build();
            }
        }
        // save user
        User newUser = User.builder()
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .otherName(customerRequest.getOtherName())
                .gender(customerRequest.getGender())
                .address(customerRequest.getAddress())
                .country(customerRequest.getCountry())
                .state(customerRequest.getState())
                .email(customerRequest.getEmail())
                .phoneNumber(customerRequest.getPhoneNumber())
                .alternativePhoneNumber(customerRequest.getAlternativePhoneNumber())
                .noOfAccounts(0)
                .totalBalance(BigDecimal.ZERO)
                .multipleAccounts(false)
                .emailVerified(false)
                .profileEnabled(false)
                .role(User.Role.CUSTOMER)
                .build();
        User savedUser = userRepository.save(newUser);
        log.info("New onboarded user name is {}:::", savedUser.getFirstName());

        credentialService.sendEmailOtp(savedUser.getFirstName(), savedUser.getEmail(), "Email Verification");
        log.info("OTP for email verification sent to user email - {}:::", savedUser.getEmail());

        return AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data("An OTP has been sent to the provided email address!")
                .build();

    }

    @Override
    public AppResponse<?> verifyCustomerEmail(String emailAddress, String otp) {
        boolean isVerified = credentialService.verifyEmailOtp(emailAddress, otp);
        if (isVerified) {
            userRepository.findByEmail(emailAddress).ifPresent(customer -> {
                customer.setEmailVerified(true);
                userRepository.save(customer);
            });

            return AppResponse.builder()
                    .responseCode(AccountResponses.SUCCESS.getCode())
                    .responseMessage(AccountResponses.SUCCESS.getMessage())
                    .data("Otp verified successfully!")
                    .build();
        }
        return AppResponse.builder()
                .responseCode(AccountResponses.FAILED.getCode())
                .responseMessage(AccountResponses.FAILED.getMessage())
                .data("Incorrect Otp!")
                .build();
    }

    @Transactional
    @Override
    public AppResponse<?> createAccountForCustomer(String emailAddress) {
        Optional<User> oCustomer = userRepository.findByEmail(emailAddress);

        if (oCustomer.isEmpty()) {
            log.info("Customer not found");
            return AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        User user = oCustomer.get();
        if (!user.isEmailVerified()) {
            log.info("User Email Is Not Verified");

            return AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data("Email Address is not verified!")
                    .build();
        }

        if (accountHelper.checkIfCustomerHasAnAccount(user.getId()) && !user.isMultipleAccounts()) {
            log.info("User Is Not Approved For Multiple Accounts!");

            return AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data("You already have an existing account!")
                    .build();
        }

        String accountNumber = accountHelper.generateUniqueAccountNumber(5);
        if (accountNumber == null) {
            log.error("Unable to generate account number for new user:::");
            return AppResponse.builder()
                .responseCode(AccountResponses.ACCOUNT_CREATION_FAILED.getCode())
                .responseMessage(AccountResponses.ACCOUNT_CREATION_FAILED.getMessage())
                .data(null)
                .build();
        }

        Account newAccount = Account.builder()
                .accountNumber(accountNumber)
                .accountBalance(BigDecimal.ZERO)
                .accountCurrency(CurrencyType.NGN)
                .accountStatus("ACTIVE")
                .user(user)
                .build();
        Account savedAccount = accountRepository.save(newAccount);

        String defaultPassword = passwordGenerator.generatePassword();
        user.setAppPassword(passwordEncoder.encode(defaultPassword));
        user.setDefaultPassword(true);
        user.setDefaultPasswordIssuedAt(LocalDateTime.now());
        user.setNoOfAccounts(user.getNoOfAccounts() + 1);
        user.setProfileEnabled(true);
        User savedUser = userRepository.save(user);

        // send email to user
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setSubject(EmailUtils.NEW_CUSTOMER_EMAIL_SUBJECT.getTemplate());
        emailDetails.setBody(EmailUtils.NEW_CUSTOMER_EMAIL_BODY
                                     .format(appName, user.getFirstName() + " " + user.getLastName(), savedAccount.getAccountNumber(),
                                             defaultPassword));
        emailDetails.setRecipient(user.getEmail());
        eventPublisher.publishEvent(new EmailEvent(emailDetails));

        log.info("Sending onboarding mail to user - {}:::", user.getEmail());
        return AppResponse.builder()
                .responseCode(AccountResponses.ACCOUNT_CREATION_SUCCESSFUL.getCode())
                .responseMessage(AccountResponses.ACCOUNT_CREATION_SUCCESSFUL.getMessage())
                .data(mapperClass.getCustomerDetails(savedUser, accountNumber))
                .build();
    }

    @Override
    public AppResponse<?> balanceEnquiry(EnquiryRequest request) {
        log.info("Received request to get account balance");

        boolean accountExists = accountHelper.checkIfAccountExists(request.getAccountNumber());
        if (!accountExists) {
            log.error("Account does not exist::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber());
        UUID customerId = account.getUser().getId();
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + customerId));

        // Check if the logged in customer the one making the request and is the owner of the account
        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        boolean isValidRequest = loggedInCustomerId.equals(customerId) && loggedInCustomerId.equals(UUID.fromString(request.getCustomerId()));
        if (!isValidRequest) {
            log.error("Invalid Request - Customer is not authorized to make this request!");
            return AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage("Failed: You are not authorized to make this request!")
                    .data(null)
                    .build();
        }

        log.info("Returned account data is {}", account);
        return AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data(AccountDetails.builder()
                        .accountNumber(account.getAccountNumber())
                        .accountBalance(account.getAccountBalance())
                        .accountCurrency(account.getAccountCurrency())
                        .accountStatus(account.getAccountStatus())
                        .accountName(accountHelper.getCustomerFullName(customerId))
                        .build()
                )
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        log.info("Received request to get account name::");

        // Check if the logged in customer the one making the request
        UUID loggedInCustomerId = AccountUtils.getLoggedInCustomerId();
        boolean isValidRequest = loggedInCustomerId.equals(UUID.fromString(request.getCustomerId()));
        if (!isValidRequest) {
            log.error("Invalid Request - Customer is not authorized to make this request!!");
            return null;
        }

        boolean accountExists = accountHelper.checkIfAccountExists(request.getAccountNumber());
        if (!accountExists) {
            log.error("Account does not exist:::");
            return AccountResponses.ACCOUNT_NOT_FOUND.getMessage();
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber());
        UUID customerId = account.getUser().getId();
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + customerId));

        log.info("Returned user first name is {}:::", user.getFirstName());
        return accountHelper.getCustomerFullName(customerId);
    }
}
