package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.AccountDetails;
import com.zainab.PearsonBank.dto.CustomerDetails;
import com.zainab.PearsonBank.dto.TransactionDetails;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.entity.User;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.TransactionRepository;
import com.zainab.PearsonBank.repository.UserRepository;
import com.zainab.PearsonBank.service.AdminService;
import com.zainab.PearsonBank.types.TransactionStatus;
import com.zainab.PearsonBank.utils.MapperClass;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    MapperClass mapper;


    @Override
    public List<AccountDetails> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(mapper::getAccountDetails)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDetails getSingleAccount(String accountId) {
        Optional<Account> acc = accountRepository.findById(UUID.fromString(accountId));
        return acc.map(account -> mapper.getAccountDetails(account)).orElse(null);
    }

    @Override
    public boolean activateAccount(String accountId) {
        try {
            Optional<Account> acc = accountRepository.findById(UUID.fromString(accountId));
            if (acc.isPresent()) {
                Account account = acc.get();
                if (account.getAccountStatus().equals("Active")) return true;

                account.setAccountStatus("Active");
                accountRepository.save(account);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deactivateAccount(String accountId) {
        try {
            Optional<Account> acc = accountRepository.findById(UUID.fromString(accountId));
            if (acc.isPresent()) {
                Account account = acc.get();
                if (account.getAccountStatus().equals("Inactive")) return true;

                account.setAccountStatus("Inactive");
                accountRepository.save(account);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<CustomerDetails> getAllCustomers() {
        return userRepository.findAll()
                .stream()
                .map(mapper::getCustomerDetails)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDetails getSingleCustomer(String customerId) {
        Optional<User> cust = userRepository.findById(UUID.fromString(customerId));
        return cust.map(customer -> mapper.getCustomerDetails(customer)).orElse(null);
    }

    @Override
    public boolean disableUser(String customerId) {
        try {
            Optional<User> u = userRepository.findById(UUID.fromString(customerId));
            if (u.isPresent()) {
                User user = u.get();
                if (!user.isProfileEnabled()) return true;

                user.setProfileEnabled(false);
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean enableUser(String customerId) {
        try {
            Optional<User> u = userRepository.findById(UUID.fromString(customerId));
            if (u.isPresent()) {
                User user = u.get();
                if (user.isProfileEnabled()) return true;

                user.setProfileEnabled(true);
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<TransactionDetails> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(mapper::getTransactionDetails)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDetails getSingleTransaction(String transactionId) {
         Optional<Transaction> txn = transactionRepository.findById(UUID.fromString(transactionId));
        return txn.map(transaction ->  mapper.getTransactionDetails(transaction))
                .orElse(null);
    }

    @Override
    public boolean reverseTransaction(String transactionId) {
        try {
            Optional<Transaction> t = transactionRepository.findById(UUID.fromString(transactionId));
            if (t.isPresent()) {
                Transaction txn = t.get();
                String amt = txn.getAmount();
                String beneficiaryAcc = txn.getCrAccountNumber();
                String senderAcc = txn.getDrAccountNumber();

                reverseTxn(beneficiaryAcc, senderAcc, amt);

                txn.setTransactionStatus(TransactionStatus.REVERSED);
                transactionRepository.save(txn);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    private void reverseTxn(String beneficiaryAccNo, String senderAccNo, String amount) {
        try {
            // get sender and beneficiary accounts
            Account senderAcc = accountRepository.findByAccountNumber(senderAccNo);
            User sender = userRepository.findById(senderAcc.getUser().getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));

            Account beneficiaryAcc = accountRepository.findByAccountNumber(beneficiaryAccNo);
            User beneficiary = userRepository.findById(beneficiaryAcc.getUser().getId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found!"));

            // credit sender and debit beneficiary
            BigDecimal newSenderBalance = senderAcc.getAccountBalance().add(new BigDecimal(amount));
            BigDecimal newSenderTotalBalance = sender.getTotalBalance().add(new BigDecimal(amount));

            senderAcc.setAccountBalance(newSenderBalance);
            sender.setTotalBalance(newSenderTotalBalance);
            accountRepository.save(senderAcc);
            userRepository.save(sender);

            BigDecimal newBeneficiaryBalance = beneficiaryAcc.getAccountBalance().subtract(new BigDecimal(amount));
            BigDecimal newBeneficiaryTotalBalance = beneficiary.getTotalBalance().subtract(new BigDecimal(amount));

            beneficiaryAcc.setAccountBalance(newBeneficiaryBalance);
            beneficiary.setTotalBalance(newBeneficiaryTotalBalance);
            accountRepository.save(beneficiaryAcc);
            userRepository.save(beneficiary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
