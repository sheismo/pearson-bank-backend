package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Customer;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.repository.TransactionRepository;
import com.zainab.PearsonBank.service.AdminService;
import com.zainab.PearsonBank.types.TransactionStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    TransactionRepository transactionRepository;


    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    public Account getSingleAccount(String accountId) {
        Optional<Account> acc = accountRepository.findById(UUID.fromString(accountId));
        return acc.orElse(null);
    }

    @Override
    public boolean activateAccount(String accountId) {
        try {
            Optional<Account> acc = accountRepository.findById(UUID.fromString(accountId));
            if (acc.isPresent()) {
                Account account = acc.get();
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
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer getSingleCustomer(String customerId) {
        Optional<Customer> cust = customerRepository.findById(UUID.fromString(customerId));
        return cust.orElse(null);
    }

    @Override
    public boolean disableUser(String customerId) {
        try {
            Optional<Customer> c = customerRepository.findById(UUID.fromString(customerId));
            if (c.isPresent()) {
                Customer customer = c.get();
                customer.setProfileEnabled(false);
                customerRepository.save(customer);
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
            Optional<Customer> c = customerRepository.findById(UUID.fromString(customerId));
            if (c.isPresent()) {
                Customer customer = c.get();
                customer.setProfileEnabled(true);
                customerRepository.save(customer);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Transaction getSingleTransaction(String transactionId) {
         Optional<Transaction> txn = transactionRepository.findById(UUID.fromString(transactionId));
        return txn.orElse(null);
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
            Customer sender = customerRepository.findById(senderAcc.getCustomer().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found!"));

            Account beneficiaryAcc = accountRepository.findByAccountNumber(beneficiaryAccNo);
            Customer beneficiary = customerRepository.findById(beneficiaryAcc.getCustomer().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer not found!"));

            // credit sender and debit beneficiary
            BigDecimal newSenderBalance = senderAcc.getAccountBalance().add(new BigDecimal(amount));
            BigDecimal newSenderTotalBalance = sender.getTotalBalance().add(new BigDecimal(amount));

            senderAcc.setAccountBalance(newSenderBalance);
            sender.setTotalBalance(newSenderTotalBalance);
            accountRepository.save(senderAcc);
            customerRepository.save(sender);

            BigDecimal newBeneficiaryBalance = beneficiaryAcc.getAccountBalance().subtract(new BigDecimal(amount));
            BigDecimal newBeneficiaryTotalBalance = beneficiary.getTotalBalance().subtract(new BigDecimal(amount));

            beneficiaryAcc.setAccountBalance(newBeneficiaryBalance);
            beneficiary.setTotalBalance(newBeneficiaryTotalBalance);
            accountRepository.save(beneficiaryAcc);
            customerRepository.save(beneficiary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
