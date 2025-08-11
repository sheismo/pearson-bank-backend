package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.AccountInfo;
import com.zainab.PearsonBank.dto.AppResponse;
import com.zainab.PearsonBank.dto.CustomerRequest;
import com.zainab.PearsonBank.dto.EmailDetails;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.service.CustomerService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CustomerServiceImpl implements CustomerService  {
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    EmailService emailService;

    @Override
    public AppResponse createAccount(CustomerRequest customerRequest) {
        /**
         * check if user has an existing account
         * generate a unique account number
         * create a new customer account - save details to db
         * return appropriate response to user
         */
        if (customerRepository.existsByEmail(customerRequest.getEmail())) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_EXISTS.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_EXISTS.getMessage())
                    .accountInfo(null)
                    .build();
        }

        String accountNumber = AccountUtils.generateUniqueAccountNumber(customerRepository, 5);
        if (accountNumber == null) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_CREATION_FAILED.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_CREATION_FAILED.getMessage())
                    .accountInfo(null)
                    .build();
        }

        Customer newCustomer = Customer.builder()
                .firstName(customerRequest.getFirstName())
                .lastName(customerRequest.getLastName())
                .otherName(customerRequest.getOtherName())
                .gender(customerRequest.getGender())
                .address(customerRequest.getAddress())
                .stateOfOrigin(customerRequest.getStateOfOrigin())
                .accountNumber(AccountUtils.generateAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .email(customerRequest.getEmail())
                .phoneNumber(customerRequest.getPhoneNumber())
                .alternativePhoneNumber(customerRequest.getAlternativePhoneNumber())
                .status("ACTIVE")
                .build();

        Customer savedCustomer = customerRepository.save(newCustomer);

        // send welcome email to customer
        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setSubject("");
        emailDetails.setBody("");
        emailDetails.setRecipient(savedCustomer.getEmail());
        emailService.sendEmailAlert(savedCustomer.getEmail());
        return AppResponse.builder()
                .responseCode(AccountResponses.ACCOUNT_CREATION_SUCCESSFUL.getCode())
                .responseMessage(AccountResponses.ACCOUNT_CREATION_SUCCESSFUL.getMessage())
                .accountInfo(AccountInfo.builder()
                        .accountName(savedCustomer.getFirstName() + " " + savedCustomer.getLastName())
                        .accountNumber(savedCustomer.getAccountNumber())
                        .accountBalance(savedCustomer.getAccountBalance())
                        .linkedEmail(savedCustomer.getEmail())
                        .linkedPhone(savedCustomer.getPhoneNumber())
                        .build()
                )
                .build();
    }
}
