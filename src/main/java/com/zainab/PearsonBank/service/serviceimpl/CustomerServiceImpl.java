package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.CustomerRepository;
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
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService  {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final TransactionService transactionService;
    private final AccountHelper accountHelper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public AppResponse<?> createAccount(CustomerRequest customerRequest) {
        log.info("Received request to open new customer account");
        /**
         * check if user has an existing account
         * generate a unique account number
         * create a new customer account - save details to db
         * send welcome email to user
         * return appropriate response to user
         */
        if (accountHelper.checkIfCustomerExistsByEmail(customerRequest.getEmail())) {
            log.error("Customer already has an existing account registered to this email provided!");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_EXISTS.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_EXISTS.getMessage())
                    .data(null)
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
                .noOfAccounts(1)
                .totalBalance(BigDecimal.ZERO)
                .build();
        Customer savedCustomer = customerRepository.save(newCustomer);

        // save account
        Account newAccount = Account.builder()
                .accountNumber(accountNumber)
                .accountBalance(BigDecimal.ZERO)
                .accountCurrency(CurrencyType.NGN)
                .accountStatus("ACTIVE")
                .customer(savedCustomer)
                .build();
        Account savedAccount = accountRepository.save(newAccount);

        log.info("New onboarded customer name is {}, account number is {}:::", savedCustomer.getFirstName(), savedAccount.getAccountNumber());

        // send email to customer
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setSubject(EmailUtils.NEW_CUSTOMER_EMAIL_SUBJECT.getTemplate());
        emailDetails.setBody(EmailUtils.NEW_CUSTOMER_EMAIL_BODY
                .format(savedCustomer.getFirstName() + " " + savedCustomer.getLastName(), savedAccount.getAccountNumber()));
        emailDetails.setRecipient(savedCustomer.getEmail());
        eventPublisher.publishEvent(
                new EmailEvent(emailDetails)
        );
        log.info("Onboarding mail sent to customer email - {}:::", savedCustomer.getEmail());

        return AppResponse.builder()
                .responseCode(AccountResponses.ACCOUNT_CREATION_SUCCESSFUL.getCode())
                .responseMessage(AccountResponses.ACCOUNT_CREATION_SUCCESSFUL.getMessage())
                .data(AccountDetails.builder()
                        .accountName(accountHelper.getCustomerFullName(savedCustomer.getId()))
                        .accountNumber(savedAccount.getAccountNumber())
                        .accountBalance(savedAccount.getAccountBalance())
                        .accountCurrency(savedAccount.getAccountCurrency())
                        .accountStatus(savedAccount.getAccountStatus())
                        .linkedEmail(savedCustomer.getEmail())
                        .linkedPhone(savedCustomer.getPhoneNumber())
                        .build()
                )
                .build();
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
