package com.zainab.PearsonBank.service.serviceimpl;

import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.User;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.UserRepository;
import com.zainab.PearsonBank.repository.TransactionRepository;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.types.TransactionStatus;
import com.zainab.PearsonBank.types.TransactionType;
import com.zainab.PearsonBank.utils.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final AccountHelper accountHelper;
    private final PdfGenerator pdfGenerator;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.name}")
    private String defaultNarration;

    @Transactional
    @Override
    public AppResponse<?> creditAccount(CreditDebitRequest creditRequest) {
        log.info("Received request to credit account with ac/no {}", creditRequest.getAccountNumber());

        // extract all parameters
        String crAccountNo = creditRequest.getAccountNumber();
        BigDecimal crAmount = creditRequest.getAmount();
        UUID customerId = creditRequest.getCustomerId(); //get logged in user account
        String channel = creditRequest.getChannel() != null ? creditRequest.getChannel() : "web";
        String narration = creditRequest.getNarration() != null ? defaultNarration + creditRequest.getChannel() : defaultNarration;
        String ip = creditRequest.getSenderIp();

        Account account = accountRepository.findByAccountNumber(crAccountNo);
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + customerId));
        log.info("Account no is: {}, User name is: {}", account.getAccountNumber(), accountHelper.getCustomerFullName(user));

        // TODO verify request
        // check if the user id passed is same as user id of the logged-in user
        // check if debit acc/no passed in the request is the same as acc/no of the logged-in user
//        boolean isLoggedInCustomerMakingRequest; customerId().equals(loggedInCustomerId)
//        boolean isLoggedInCustomerOwnerOfAccount = account.getUser().getId().equals(loggedInCustomerId);
//        boolean isCustomerMakingRequestOwnerOfAccount = account.getUser().getId().equals(customerId);

        // check if account number exists
        boolean accountExists = accountHelper.checkIfAccountExists(crAccountNo);
        if (!accountExists) {
            log.error("Account does not exist:::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        // check if account is active
        boolean accountIsActive = accountHelper.checkIfAccountIsActive(crAccountNo);
        if (!accountIsActive) {
            log.error("Account is inactive:::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_INACTIVE.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_INACTIVE.getMessage())
                    .data(null)
                    .build();
        }

        log.info("Crediting account - acc number: {}, amount: {}", creditRequest.getAccountNumber(), crAmount);
        // proceed to credit
        Transaction txn = new Transaction();
        txn.setAmount(String.valueOf(crAmount));
        txn.setCrAccountName(accountHelper.getCustomerFullName(user));
        txn.setCrAccountNumber(crAccountNo);
        txn.setDrAccountName("system");
        txn.setDrAccountNumber("system");

        LocalDateTime transactionDate = LocalDateTime.now();
        txn.setCreatedDate(transactionDate);

        txn.setInitiator(customerId);
        txn.setInitiatorIp(ip);
        txn.setType(TransactionType.DEPOSIT);
        txn.setChannel(channel);
        txn.setNarration(narration);
        txn.setTransactionStatus(TransactionStatus.PROCESSING);

        Transaction savedTxn = transactionRepository.save(txn);
        String txnReference = accountHelper.generateReference(savedTxn.getTransactionId(), customerId);
        log.info("Transaction reference for ongoing transaction is {} :::", txnReference);

        try {
            BigDecimal newBalance = account.getAccountBalance().add(crAmount);
            account.setAccountBalance(newBalance);
            accountRepository.save(account);

            BigDecimal newTotalBalance = user.getTotalBalance().add(crAmount);
            user.setTotalBalance(newTotalBalance);
            userRepository.save(user);

            savedTxn.setReferenceNo(txnReference);
            savedTxn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
            transactionRepository.save(savedTxn);

            // send credit email alert
            String formattedDate = transactionDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));

            EmailDetails depositEmail = new EmailDetails();
            depositEmail.setSubject(EmailUtils.NEW_TRANSACTION_DEPOSIT_ALERT_SUBJECT.getTemplate());
            depositEmail.setBody(EmailUtils.NEW_TRANSACTION_DEPOSIT_ALERT_BODY.format(
                    accountHelper.getCustomerFullName(user), "₦" + crAmount.toPlainString(), formattedDate
            ));
            depositEmail.setRecipient(user.getEmail());
            eventPublisher.publishEvent(
                    new EmailEvent(depositEmail)
            );

            log.info("Credit successful!:::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_CREDIT_SUCCESS.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_CREDIT_SUCCESS.getMessage())
                    .data(AccountDetails.builder()
                            .accountId(account.getId())
                            .ownerId(customerId)
                            .accountName(accountHelper.getCustomerFullName(customerId))
                            .accountBalance(account.getAccountBalance())
                            .accountNumber(account.getAccountNumber())
                            .accountCurrency(account.getAccountCurrency())
                            .accountStatus(account.getAccountStatus())
                            .linkedEmail(user.getEmail())
                            .linkedPhone(user.getPhoneNumber())
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("Error occurred while crediting account - {}:::", e.getMessage(), e.getCause());

            savedTxn.setReferenceNo(txnReference);
            savedTxn.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTxn);

            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_CREDIT_FAILED.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_CREDIT_FAILED.getMessage())
                    .data(AccountDetails.builder()
                            .accountId(account.getId())
                            .ownerId(customerId)
                            .accountName(accountHelper.getCustomerFullName(customerId))
                            .accountBalance(account.getAccountBalance())
                            .accountNumber(account.getAccountNumber())
                            .linkedEmail(user.getEmail())
                            .linkedPhone(user.getPhoneNumber())
                            .build())
                    .build();
        }
    }

    @Transactional
    @Override
    public AppResponse<?> debitAccount(CreditDebitRequest debitRequest) {
        log.info("Received request to debit account with ac/no {}", debitRequest.getAccountNumber());

        // extract all parameters
        String drAccountNo = debitRequest.getAccountNumber();
        BigDecimal drAmount = debitRequest.getAmount();
        UUID customerId = debitRequest.getCustomerId();
        String channel = debitRequest.getChannel() != null ? debitRequest.getChannel() : "web";
        String narration = debitRequest.getNarration() != null ? defaultNarration + debitRequest.getChannel() : defaultNarration;
        String ip = debitRequest.getSenderIp();

        Account account = accountRepository.findByAccountNumber(drAccountNo);
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + customerId));

        // TODO verify request
        // check if the user id passed is same as user id of the logged-in user
        // check if debit acc/no passed in the request is the same as acc/no of the logged-in user
//        boolean isLoggedInCustomerMakingRequest; debitRequest.getCustomerId().equals(loggedInCustomerId)
//        boolean isLoggedInCustomerOwnerOfAccount = account.getUser().getId().equals(loggedInCustomerId);
//        boolean isCustomerMakingRequestOwnerOfAccount = account.getUser().getId().equals(customerId);

        // check if account exists
        boolean accountExists = accountHelper.checkIfAccountExists(drAccountNo);
        if (!accountExists) {
            log.error("Account does not exist::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        // check if account is active
        boolean accountIsActive = accountHelper.checkIfAccountIsActive(drAccountNo);
        if (!accountIsActive) {
            log.info("Account - {} is not active:::", drAccountNo);
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_INACTIVE.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_INACTIVE.getMessage())
                    .data(null)
                    .build();
        }

        // check for insufficient funds
        boolean sufficientFunds = AccountUtils.hasSufficientBalance(account.getAccountBalance(), drAmount);
        if (!sufficientFunds) {
            log.info("Insufficient funds for this account:::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.INSUFFICIENT_FUNDS.getCode())
                    .responseMessage(AccountResponses.INSUFFICIENT_FUNDS.getMessage())
                    .data(AccountDetails.builder()
                            .accountNumber(account.getAccountNumber())
                            .accountName(accountHelper.getCustomerFullName(user))
                            .accountBalance(account.getAccountBalance())
                            .build())
                    .build();
        }

        // proceed to debit
        log.info("Debiting account - acc number: {}, amount: {}", drAccountNo, drAmount);
        Transaction txn = new Transaction();
        txn.setAmount(String.valueOf(drAmount));
        txn.setDrAccountName(accountHelper.getCustomerFullName(user));
        txn.setDrAccountNumber(drAccountNo);
        txn.setCrAccountName("system");
        txn.setCrAccountNumber("system");

        LocalDateTime transactionDate = LocalDateTime.now();
        txn.setCreatedDate(transactionDate);

        txn.setInitiator(customerId);
        txn.setInitiatorIp(ip);
        txn.setType(TransactionType.WITHDRAWAL);
        txn.setChannel(channel);
        txn.setNarration(narration);
        txn.setTransactionStatus(TransactionStatus.PROCESSING);

        Transaction savedTxn = transactionRepository.save(txn);
        String txnReference = accountHelper.generateReference(savedTxn.getTransactionId(), customerId);
        log.info("Transaction reference for ongoing transaction is {} ::::", txnReference);

        try {
            // debit account and send transaction email
            BigDecimal newBalance = account.getAccountBalance().subtract(drAmount);
            account.setAccountBalance(newBalance);
            accountRepository.save(account);

            BigDecimal newTotalBalance = user.getTotalBalance().subtract(drAmount);
            user.setTotalBalance(newTotalBalance);
            userRepository.save(user);

            savedTxn.setReferenceNo(txnReference);
            savedTxn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
            transactionRepository.save(savedTxn);

            // send debit email alert
            String formattedDate = transactionDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));

            EmailDetails withdrawalEmail = new EmailDetails();
            withdrawalEmail.setSubject(EmailUtils.NEW_TRANSACTION_WITHDRAWAL_ALERT_SUBJECT.getTemplate());
            withdrawalEmail.setBody(EmailUtils.NEW_TRANSACTION_WITHDRAWAL_ALERT_BODY.format(
                    accountHelper.getCustomerFullName(user), "₦" + drAmount.toPlainString(), formattedDate
            ));
            withdrawalEmail.setRecipient(user.getEmail());
            eventPublisher.publishEvent(
                    new EmailEvent(withdrawalEmail)
            );

            log.info("Debit successful!:::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_DEBIT_SUCCESS.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_DEBIT_SUCCESS.getMessage())
                    .data(AccountDetails.builder()
                            .accountId(account.getId())
                            .ownerId(customerId)
                            .accountName(accountHelper.getCustomerFullName(customerId))
                            .accountBalance(account.getAccountBalance())
                            .accountNumber(account.getAccountNumber())
                            .accountCurrency(account.getAccountCurrency())
                            .accountStatus(account.getAccountStatus())
                            .linkedEmail(user.getEmail())
                            .linkedPhone(user.getPhoneNumber())
                            .build())
                    .build();

        } catch (Exception e) {
            savedTxn.setReferenceNo(txnReference);
            savedTxn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
            transactionRepository.save(savedTxn);

            log.error("Error occurred while debiting account:::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_DEBIT_FAILED.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_DEBIT_FAILED.getMessage())
                    .data(AccountDetails.builder()
                            .accountId(account.getId())
                            .ownerId(customerId)
                            .accountName(accountHelper.getCustomerFullName(customerId))
                            .accountBalance(account.getAccountBalance())
                            .accountNumber(account.getAccountNumber())
                            .linkedEmail(user.getEmail())
                            .linkedPhone(user.getPhoneNumber())
                            .build())
                    .build();
        }
    }

    @Transactional
    @Override
    public AppResponse<?> transferMoney(TransferRequest transferRequest) {
        log.info("Received request to transfer funds - {} ::::::", transferRequest);

        // extract all parameters
        String drAccountNo = transferRequest.getDrAccountNumber();
        String crAccountNo = transferRequest.getCrAccountNumber();
        BigDecimal transferAmount = transferRequest.getAmount();
        UUID customerId = UUID.fromString(transferRequest.getCustomerId());
        String channel = transferRequest.getChannel() != null ? transferRequest.getChannel() : "web";
        String narration = transferRequest.getNarration() != null ? defaultNarration + transferRequest.getChannel() : defaultNarration;
        String ip = transferRequest.getSenderIp();

        // get sender and beneficiary accounts
        Account senderAcc = accountRepository.findByAccountNumber(drAccountNo);
        User sender = userRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + customerId));

        Account beneficiaryAcc = accountRepository.findByAccountNumber(crAccountNo);
        User beneficiary = userRepository.findById(beneficiaryAcc.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + beneficiaryAcc.getUser().getId()));

        // TODO get logged-in user details
        // check if the user id passed is same as user id of the logged-in user
        // check if debit acc/no passed in the request is the same as acc/no of the logged-in user
//        boolean isLoggedInCustomerMakingRequest; transferRequest.getCustomerId().equals(loggedInCustomerId)
//        boolean isLoggedInCustomerOwnerOfAccount = senderAcc.getUser().getId().equals(loggedInCustomerId);
//        boolean isCustomerMakingRequestOwnerOfAccount = senderAcc.getUser().getId().equals(customerId);

        // check if accounts exist
        boolean drAccountExists = accountHelper.checkIfAccountExists(drAccountNo);
        boolean crAccountExists = accountHelper.checkIfAccountExists(crAccountNo);
        if (!drAccountExists || !crAccountExists) {
            log.error("Account existence check failed: crAccountNo={} exists={}, drAccountNo={} exists={}",
                    crAccountNo, crAccountExists, drAccountNo, drAccountExists);
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        // check if accounts are active
        boolean drAccountActive = accountHelper.checkIfAccountIsActive(drAccountNo);
        boolean crAccountActive = accountHelper.checkIfAccountIsActive(crAccountNo);
        if (!drAccountActive || !crAccountActive) {
            log.error("Account status check failed: crAccountNo={} isActive={}, drAccountNo={} isActive={}",
                    crAccountNo, crAccountActive, drAccountNo, drAccountActive);
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_INACTIVE.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_INACTIVE.getMessage())
                    .data(null)
                    .build();
        }

        // check for insufficient funds
        boolean sufficientFunds = AccountUtils.hasSufficientBalance(senderAcc.getAccountBalance(), transferAmount);
        if (!sufficientFunds) {
            log.info("Insufficient funds for this account::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.INSUFFICIENT_FUNDS.getCode())
                    .responseMessage(AccountResponses.INSUFFICIENT_FUNDS.getMessage())
                    .data(TransferResponse.builder()
                            .senderName(accountHelper.getCustomerFullName(customerId))
                            .senderBalance(String.valueOf(senderAcc.getAccountBalance()))
                            .senderAccount(senderAcc.getAccountNumber())
                            .beneficiaryName(accountHelper.getCustomerFullName(beneficiary))
                            .beneficiaryAccount(beneficiaryAcc.getAccountNumber())
                            .txnAmount(String.valueOf(transferAmount))
                            .txnReference("")
                            .txnStatus(TransactionStatus.INCOMPLETE)
                            .build())
                    .build();
        }

        log.info("Processing request to transfer NGN {} from account 1 [{}] to  account 2 [{}] by user with id {}",
                transferAmount, drAccountNo, crAccountNo, customerId);

        // proceed to transfer funds
        Transaction txn = new Transaction();
        txn.setAmount(String.valueOf(transferAmount));
        txn.setCrAccountName(accountHelper.getCustomerFullName(beneficiary.getId()));
        txn.setCrAccountNumber(crAccountNo);
        txn.setDrAccountName(accountHelper.getCustomerFullName(sender.getId()));
        txn.setDrAccountNumber(drAccountNo);

        LocalDateTime transactionDate = LocalDateTime.now();
        txn.setCreatedDate(transactionDate);

        txn.setInitiator(sender.getId());
        txn.setInitiatorIp(ip);
        txn.setType(TransactionType.TRANSFER);
        txn.setChannel(channel);
        txn.setNarration(narration);
        txn.setTransactionStatus(TransactionStatus.PROCESSING);

        Transaction savedTxn = transactionRepository.save(txn);
        String txnReference = accountHelper.generateReference(savedTxn.getTransactionId(), sender.getId());

        try {
            // debit sender and credit beneficiary
            BigDecimal newSenderBalance = senderAcc.getAccountBalance().subtract(transferAmount);
            BigDecimal newSenderTotalBalance = sender.getTotalBalance().subtract(transferAmount);

            senderAcc.setAccountBalance(newSenderBalance);
            sender.setTotalBalance(newSenderTotalBalance);
            accountRepository.save(senderAcc);
            userRepository.save(sender);

            BigDecimal newBeneficiaryBalance = beneficiaryAcc.getAccountBalance().add(transferAmount);
            BigDecimal newBeneficiaryTotalBalance = beneficiary.getTotalBalance().add(transferAmount);

            beneficiaryAcc.setAccountBalance(newBeneficiaryBalance);
            beneficiary.setTotalBalance(newBeneficiaryTotalBalance);
            accountRepository.save(beneficiaryAcc);
            userRepository.save(beneficiary);

            // send transaction emails
            String formattedDate = transactionDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));

            EmailDetails debitEmail = new EmailDetails();
            debitEmail.setSubject(EmailUtils.NEW_TRANSACTION_DEBIT_ALERT_SUBJECT.getTemplate());
            debitEmail.setBody(EmailUtils.NEW_TRANSACTION_DEBIT_ALERT_BODY.format(
                    accountHelper.getCustomerFullName(sender), "₦" + transferAmount.toPlainString(), accountHelper.getCustomerFullName(beneficiary), formattedDate
            ));
            debitEmail.setRecipient(sender.getEmail());
            eventPublisher.publishEvent(
                    new EmailEvent(debitEmail)
            );

            EmailDetails creditEmail = new EmailDetails();
            creditEmail.setSubject(EmailUtils.NEW_TRANSACTION_CREDIT_ALERT_SUBJECT.getTemplate());
            creditEmail.setBody(EmailUtils.NEW_TRANSACTION_CREDIT_ALERT_BODY.format(
                    accountHelper.getCustomerFullName(beneficiary), "₦" + transferAmount.toPlainString(), accountHelper.getCustomerFullName(sender), formattedDate
            ));
            creditEmail.setRecipient(beneficiary.getEmail());
            eventPublisher.publishEvent(
                    new EmailEvent(creditEmail)
            );

            // save transaction reference and status
            savedTxn.setReferenceNo(txnReference);
            savedTxn.setTransactionStatus(TransactionStatus.SUCCESSFUL);
            transactionRepository.save(savedTxn);

            // return response
            log.info("Transfer successful:::::::::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.FUNDS_TRANSFER_SUCCESSFUL.getCode())
                    .responseMessage(AccountResponses.FUNDS_TRANSFER_SUCCESSFUL.getMessage())
                    .data(TransferResponse.builder()
                            .senderName(accountHelper.getCustomerFullName(customerId))
                            .senderBalance(String.valueOf(senderAcc.getAccountBalance()))
                            .senderAccount(senderAcc.getAccountNumber())
                            .beneficiaryAccount(beneficiaryAcc.getAccountNumber())
                            .beneficiaryName(accountHelper.getCustomerFullName(beneficiary))
                            .txnAmount(String.valueOf(transferAmount))
                            .txnReference(txnReference)
                            .txnStatus(savedTxn.getTransactionStatus())
                            .build())
                    .build();

        } catch (Exception e) {
            // save transaction reference and status
            savedTxn.setReferenceNo(txnReference);
            savedTxn.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(savedTxn);

            log.error("Error occurred while processing transfer::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.FUNDS_TRANSFER_FAILED.getCode())
                    .responseMessage(AccountResponses.FUNDS_TRANSFER_FAILED.getMessage())
                    .data(TransferResponse.builder()
                            .senderName(accountHelper.getCustomerFullName(sender))
                            .senderBalance(String.valueOf(senderAcc.getAccountBalance()))
                            .senderAccount(senderAcc.getAccountNumber())
                            .txnAmount(String.valueOf(transferAmount))
                            .txnReference(txnReference)
                            .txnStatus(savedTxn.getTransactionStatus())
                            .build())
                    .build();
        }
    }

    @Override
    public Transaction getSingleTransaction(String customerId, String accountNumber, String transactionId) {
        log.info("Received request to get single transaction for user with id {} and account {}",
                customerId, accountNumber);

        if (accountHelper.checkIfAccountBelongsToCustomer(customerId, accountNumber)) return null;

        return transactionRepository.findByTransactionId(UUID.fromString(transactionId));
    }

    @Override
    public List<Transaction> getTransactionsForCustomer(String customerId, String accountNumber) {
        log.info("Received request to get all transactions for user::::");

        if (!accountHelper.checkIfAccountBelongsToCustomer(customerId, accountNumber)) return null;

        return transactionRepository.findAllByInitiator(UUID.fromString(customerId));
    }

    @Override
    public List<Transaction> getTransactionsForCustomer(String customerId, String accountNumber, String startDate, String endDate) {
        log.info("Received request to get transactions for user from {} to {}:::", startDate, endDate);

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(LocalTime.MAX);

        if (!accountHelper.checkIfAccountBelongsToCustomer(customerId, accountNumber)) return null;

        return transactionRepository.findAllByInitiatorAndCreatedDateBetween(UUID.fromString(customerId), start, end);
    }

    @Override
    public ResponseEntity<?> generateReceiptPdf(String transactionId) {
        log.info("Received request to generate transaction receipt for user");

        // TODO get logged-in user details
        // check if the user id passed is same as user id of the logged-in user
        // check if loggedInCustomer is owner of transaction
//        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)

        Transaction transaction = transactionRepository.findByTransactionId(UUID.fromString(transactionId));
        if (transaction == null) {
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.FAILED.getCode(), AccountResponses.FAILED.getMessage(), null) );
        };

        byte[] pdfBytes = pdfGenerator.generateReceipt(transaction);
        String filename = String.format("receipt_%s.pdf", transaction.getCreatedDate());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);

    }
}
