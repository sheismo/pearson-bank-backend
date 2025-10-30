package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.service.CredentialService;
import com.zainab.PearsonBank.service.CustomerService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.types.CurrencyType;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.EmailUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService  {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TransactionService transactionService;
    private final CredentialService credentialService;
    private final AccountHelper accountHelper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * To onboard a new customer:
     * check if user has an existing account
     * save user details and do email verification
     * verify email and generate unique account no
     * send welcome mail to user email address
     */

    @Transactional
    @Override
    public AppResponse<?> onboardNewCustomer(CustomerRequest customerRequest) {
        log.info("Received request to onboard new customer:::::");
        if (accountHelper.checkIfCustomerExistsByEmail(customerRequest.getEmail())) {
            log.error("Customer already has an existing account registered to this email provided!");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_EXISTS.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_EXISTS.getMessage())
                    .data(null)
                    .build();
        }

        try {
            // save customer
            Customer newCustomer = Customer.builder()
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
                    .emailVerified(false)
                    .isUserLocked(true)
                    .build();
            Customer savedCustomer = customerRepository.save(newCustomer);
            log.info("New onboarded customer name is {}:::", savedCustomer.getFirstName());

            credentialService.sendOtp(savedCustomer.getEmail());
            log.info("OTP for email verification sent to customer email - {}:::", savedCustomer.getEmail());

            return AppResponse.builder()
                    .responseCode(AccountResponses.SUCCESS.getCode())
                    .responseMessage(AccountResponses.SUCCESS.getMessage())
                    .data("An OTP has been sent to the provided email address!")
                    .build();
        } catch(Exception e) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.FAILED.getCode())
                    .responseMessage(AccountResponses.FAILED.getMessage())
                    .data("An error occurred!")
                    .build();
        }
    }

    @Override
    public AppResponse<?> verifyCustomerEmail(String emailAddress, String otp) {
        boolean isVerified = credentialService.verifyOtp(emailAddress, otp);
        if (isVerified) {
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

    @Override
    public AppResponse<?> createAccountForCustomer(String emailAddress) {
        Optional<Customer> oCustomer = customerRepository.findByEmail(emailAddress);
        if (oCustomer.isEmpty()) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }
        Customer customer = oCustomer.get();

        if (!customer.isEmailVerified()) {
            log.info("Customer Email Verified: {}", customer.isEmailVerified());

            return AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data("Email Address is not verified!")
                    .build();
        }

        String accountNumber = accountHelper.generateUniqueAccountNumber(5);
        if (accountNumber == null) {
            log.error("Unable to generate account number for new customer:::");
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
                .customer(customer)
                .build();
        Account savedAccount = accountRepository.save(newAccount);

        // send email to customer
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setSubject(EmailUtils.NEW_CUSTOMER_EMAIL_SUBJECT.getTemplate());
        emailDetails.setBody(EmailUtils.NEW_CUSTOMER_EMAIL_BODY
                                     .format(customer.getFirstName() + " " + customer.getLastName(), savedAccount.getAccountNumber()));
        emailDetails.setRecipient(customer.getEmail());
        eventPublisher.publishEvent(new EmailEvent(emailDetails));

        log.info("Onboarding mail sent to customer email - {}:::", customer.getEmail());
        return null;
    }

    @Override
    public AppResponse<?> balanceEnquiry(EnquiryRequest request) {
        log.info("Received request to get account balance");

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
        // check if logged-in customer is owner of account number passed in request
//        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)
//        boolean isLoggedInCustomerOwnerOfAccount = account.getCustomer().getId().equals(loggedInCustomerId);

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
        UUID customerId = account.getCustomer().getId();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

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
                        .linkedPhone(customer.getPhoneNumber())
                        .linkedEmail(customer.getEmail())
                        .build()
                )
                .build();
    }

    @Override
    public String nameEnquiry(EnquiryRequest request) {
        log.info("Received request to get account name::");

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
        // check if acc/no passed in the request is the same as acc/no of the logged-in customer
//        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)

        boolean accountExists = accountHelper.checkIfAccountExists(request.getAccountNumber());
        if (!accountExists) {
            log.error("Account does not exist:::");
            return AccountResponses.ACCOUNT_NOT_FOUND.getMessage();
        }

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber());
        UUID customerId = account.getCustomer().getId();
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + customerId));

        log.info("Returned customer first name is {}:::", customer.getFirstName());
        return accountHelper.getCustomerFullName(customerId);
    }
}
