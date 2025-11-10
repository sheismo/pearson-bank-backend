package com.zainab.PearsonBank.service.serviceimpl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zainab.PearsonBank.dto.*;
import com.zainab.PearsonBank.entity.Account;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.event.EmailEvent;
import com.zainab.PearsonBank.repository.AccountRepository;
import com.zainab.PearsonBank.repository.CustomerRepository;
import com.zainab.PearsonBank.service.AccountService;
import com.zainab.PearsonBank.service.EmailService;
import com.zainab.PearsonBank.service.TransactionService;
import com.zainab.PearsonBank.utils.AccountHelper;
import com.zainab.PearsonBank.utils.AccountResponses;
import com.zainab.PearsonBank.utils.EmailUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final TransactionService transactionService;
    private final AccountHelper accountHelper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${app.name}")
    private String appName;

    @Value("${app.tag}")
    private String appTag;

    @Value("${app.supportMail}")
    private String appSupportMail;

    @Override
    public AppResponse<?> getAccount(String accountId) {
        log.info("Received request to get customer account:::");

        // TODO get logged-in user details
        // check if the account id passed is owned by the logged-in customer
//        boolean isLoggedInCustomerMakingRequest; accountId.getCustomerId().equals(loggedInCustomerId)

        boolean accountExists = accountHelper.checkIfAccountExistsById(accountId);
        if (!accountExists) {
            log.error("Account does not exist::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        Account account = accountRepository.findById(UUID.fromString(accountId)).orElse(null);
        return AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data(account)
                .build();
    }

    @Override
    public AppResponse<?> getAccounts(GetAccountsRequest request) {
        log.info("Received request to get customer accounts:::");

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
//        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)

        String customerId = request.getCustomerId();
        boolean customerExists = accountHelper.checkIfCustomerExistsById(customerId);
        if (!customerExists) {
            log.error("Customer does not exist::::");
            return AppResponse.builder()
                    .responseCode(AccountResponses.CUSTOMER_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.CUSTOMER_NOT_FOUND.getMessage())
                    .data(null)
                    .build();
        }

        List<Account> accounts = accountRepository.findByCustomerId(UUID.fromString(customerId));
        if (accounts == null || accounts.isEmpty()) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_NOT_FOUND.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_NOT_FOUND.getMessage() + " for tis customer")
                    .data(null)
                    .build();
        }

        return AppResponse.builder()
                .responseCode(AccountResponses.SUCCESS.getCode())
                .responseMessage(AccountResponses.SUCCESS.getMessage())
                .data(accounts)
                .build();
    }

    @Override
    public ResponseEntity<?> generateAccountStatement(String customerId, String accountNumber, String startDate, String endDate) {
        log.info("Received request to generate account statement for customer with id {} and account {}",
                customerId, accountNumber);

        // TODO get logged-in user details
        // check if the customer id passed is same as customer id of the logged-in customer
        // check if loggedInCustomer is owner of account number
//        boolean isLoggedInCustomerMakingRequest; request.getCustomerId().equals(loggedInCustomerId)

        boolean accountExists = accountHelper.checkIfAccountExists(accountNumber);
        if (!accountExists) {
            log.error("Account does not exist:::::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.ACCOUNT_NOT_FOUND.getCode(), AccountResponses.ACCOUNT_NOT_FOUND.getMessage(), null) );
        }

        boolean customerExists = accountHelper.checkIfCustomerExistsById(customerId);
        if (!customerExists) {
            log.error("Customer does not exist:::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.CUSTOMER_NOT_FOUND.getCode(), AccountResponses.CUSTOMER_NOT_FOUND.getMessage(), null) );
        }

        CustomerDetails customer = accountHelper.fetchCustomerDetails(customerId);
        List<Transaction> transactions = transactionService.getTransactionsForCustomer(customerId, accountNumber, startDate, endDate);

        if (transactions == null || transactions.isEmpty()) {
            log.error("Transactions not found for customer within this date range:::");
            return ResponseEntity.badRequest().body(new AppResponse<>(AccountResponses.INVALID_REQUEST.getCode(), "Transactions not found for customer within this date range", null) );
        }

        try {
            byte[] pdfBytes = buildStatementFile(transactions, accountNumber, customer, startDate, endDate);

            String filename = String.format("statement_%s_%s_to_%s.pdf",
                    accountNumber,
                    startDate.replace("/", "-"),
                    endDate.replace("/", "-")
            );
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (FileNotFoundException | DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    @Override
    public AppResponse<?> deleteAccount(DeleteAccountRequest deleteAccountRequest) {
        log.info("Received request to delete account for customer with id {} and account with id{}",
                deleteAccountRequest.getCustomerId(), deleteAccountRequest.getAccountId());

        String accountId = deleteAccountRequest.getAccountId();
        String customerId = deleteAccountRequest.getCustomerId();

        AccountDetails accountDetails = accountHelper.fetchAccountDetails(accountId);
        CustomerDetails customerDetails = accountHelper.fetchCustomerDetails(customerId);

        if (accountHelper.checkIfAccountExistsById(accountId) || accountHelper.checkIfCustomerExistsById(customerId) ||
            accountHelper.checkIfAccountBelongsToCustomer(customerId, accountDetails.getAccountNumber())) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.INVALID_REQUEST.getCode())
                    .responseMessage(AccountResponses.INVALID_REQUEST.getMessage())
                    .data(null)
                    .build();
        }

        if (accountDetails.getAccountBalance().compareTo(BigDecimal.ZERO) != 0) {
            return AppResponse.builder()
                    .responseCode(AccountResponses.ACCOUNT_DELETION_FAILED.getCode())
                    .responseMessage(AccountResponses.ACCOUNT_DELETION_FAILED.getMessage() + " :Account balance must be exactly 0.00 to delete!")
                    .build();
        }

        if (customerDetails.getNoOfAccounts() <= 1) {
            customerRepository.deleteById(UUID.fromString(customerId)); // delete customer is that is the only account
        }
        accountRepository.deleteById(UUID.fromString(accountId));
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));

        EmailDetails accountDeletionEmail = new EmailDetails();
        accountDeletionEmail.setSubject(EmailUtils.ACCOUNT_DELETION_ALERT_SUBJECT.getTemplate());
        accountDeletionEmail.setBody(EmailUtils.ACCOUNT_DELETION_ALERT_BODY.format(
                accountHelper.getCustomerFullName(UUID.fromString(customerId)), formattedDate,
                appSupportMail, appSupportMail, appName
        ));
        accountDeletionEmail.setRecipient(customerDetails.getEmail());
        eventPublisher.publishEvent(
                new EmailEvent(accountDeletionEmail)
        );

        AppResponse<?> response = new AppResponse<>();
        response.setResponseCode(AccountResponses.ACCOUNT_DELETION_SUCCESSFUL.getCode());
        response.setResponseMessage(AccountResponses.ACCOUNT_DELETION_SUCCESSFUL.getMessage());
        return response;
    }

    private byte[] buildStatementFile(List<Transaction> transactions, String accountNumber, CustomerDetails customer, String startDate, String endDate) throws FileNotFoundException, DocumentException {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40); // Add margins
            log.info("Set document size with margins for statement pdf:::");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Define colors
            BaseColor primaryBlue = new BaseColor(49, 77, 206); // #314DCE
            BaseColor lightGray = new BaseColor(245, 245, 245); // #F5F5F5
            BaseColor darkGray = new BaseColor(51, 51, 51); // #333333
            BaseColor borderGray = new BaseColor(220, 220, 220); // #DCDCDC

            // Define fonts
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE);
            Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, darkGray);
            Font sectionTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, primaryBlue);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, darkGray);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
            Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            Font tableContentFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);

            // 1. HEADER SECTION - App Name and Tag
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            header.setSpacingAfter(20f);

            PdfPCell appNameCell = new PdfPCell(new Phrase(String.format("%s", appName), headerFont));
            appNameCell.setBorder(Rectangle.NO_BORDER);
            appNameCell.setBackgroundColor(primaryBlue);
            appNameCell.setPadding(20f);
            appNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(appNameCell);

            PdfPCell tagLineCell = new PdfPCell(new Phrase(appTag, subHeaderFont));
            tagLineCell.setBorder(Rectangle.NO_BORDER);
            tagLineCell.setBackgroundColor(lightGray);
            tagLineCell.setPadding(8f);
            tagLineCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(tagLineCell);

            // 2. STATEMENT TITLE
            Paragraph statementTitle = new Paragraph("ACCOUNT STATEMENT", sectionTitleFont);
            statementTitle.setAlignment(Element.ALIGN_CENTER);
            statementTitle.setSpacingAfter(20f);

            // 3. CUSTOMER & PERIOD DETAILS
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(25f);
            detailsTable.setWidths(new int[]{1, 1});

            // Left column - Customer details
            PdfPCell customerSection = new PdfPCell();
            customerSection.setBorder(Rectangle.NO_BORDER);
            customerSection.setPadding(15f);
            customerSection.setBackgroundColor(lightGray);

            Paragraph customerInfo = new Paragraph();
            customerInfo.add(new Chunk("Customer Information\n", labelFont));
            customerInfo.add(new Chunk("\n"));
            customerInfo.add(new Chunk("Name: ", labelFont));
            customerInfo.add(new Chunk(customer.getFullName() + "\n", valueFont));
            customerInfo.add(new Chunk("Email: ", labelFont));
            customerInfo.add(new Chunk(customer.getEmail() + "\n", valueFont));
            customerInfo.add(new Chunk("Account: ", labelFont));
            customerInfo.add(new Chunk(accountNumber != null ? accountNumber : "N/A", valueFont));
            customerSection.addElement(customerInfo);

            // Right column - Statement period
            PdfPCell periodSection = new PdfPCell();
            periodSection.setBorder(Rectangle.NO_BORDER);
            periodSection.setPadding(15f);
            periodSection.setBackgroundColor(lightGray);

            Paragraph periodInfo = new Paragraph();
            periodInfo.add(new Chunk("Statement Period\n", labelFont));
            periodInfo.add(new Chunk("\n"));
            periodInfo.add(new Chunk("From: ", labelFont));
            periodInfo.add(new Chunk(startDate + "\n", valueFont));
            periodInfo.add(new Chunk("To: ", labelFont));
            periodInfo.add(new Chunk(endDate + "\n", valueFont));
            periodInfo.add(new Chunk("Generated: ", labelFont));
            periodInfo.add(new Chunk(new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date()), valueFont));
            periodSection.addElement(periodInfo);

            detailsTable.addCell(customerSection);
            detailsTable.addCell(periodSection);

            // 4. TRANSACTIONS TABLE
            PdfPTable transactionsTable = new PdfPTable(6);
            transactionsTable.setWidthPercentage(100);
            transactionsTable.setWidths(new float[]{2f, 1.5f, 1.5f, 2f, 2f, 2.5f});
            transactionsTable.setSpacingBefore(10f);

            // Table headers
            String[] headers = {"Date", "Type", "Amount", "Sender", "Beneficiary", "Narration"};
            for (String headerText : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, tableHeaderFont));
                headerCell.setBackgroundColor(primaryBlue);
                headerCell.setBorder(Rectangle.NO_BORDER);
                headerCell.setPadding(10f);
                headerCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                transactionsTable.addCell(headerCell);
            }

            // Table content with alternating row colors
            int rowIndex = 0;
            for (Transaction t : transactions) {
                BaseColor rowColor = (rowIndex % 2 == 0) ? BaseColor.WHITE : lightGray;

                // Date
                LocalDateTime createdDate = t.getCreatedDate();
                String formattedDate = createdDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm"));
                PdfPCell dateCell = new PdfPCell(new Phrase(formattedDate, tableContentFont));
                dateCell.setBackgroundColor(rowColor);
                dateCell.setBorder(Rectangle.BOTTOM);
                dateCell.setBorderColor(borderGray);
                dateCell.setPadding(8f);
                transactionsTable.addCell(dateCell);

                // Type
                PdfPCell typeCell = new PdfPCell(new Phrase(
                        getTransactionTypeString(t.getType().ordinal()),
                        tableContentFont
                ));
                typeCell.setBackgroundColor(rowColor);
                typeCell.setBorder(Rectangle.BOTTOM);
                typeCell.setBorderColor(borderGray);
                typeCell.setPadding(8f);
                transactionsTable.addCell(typeCell);

                // Amount with color coding
                String amount = t.getAmount();
                BaseColor amountColor = t.getType().toString().contains("DEPOSIT") ?
                        new BaseColor(0, 128, 0) : // Green for credits
                        new BaseColor(220, 53, 69); // Red for debits

                PdfPCell amountCell = new PdfPCell(new Phrase(
                        "₦" + amount,
                        new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, amountColor)
                ));
                amountCell.setBackgroundColor(rowColor);
                amountCell.setBorder(Rectangle.BOTTOM);
                amountCell.setBorderColor(borderGray);
                amountCell.setPadding(8f);
                transactionsTable.addCell(amountCell);

                // Sender
                String sender = t.getDrAccountName().equals("system") ? t.getCrAccountName() : t.getDrAccountName();
                PdfPCell senderCell = new PdfPCell(new Phrase(sender, tableContentFont));
                senderCell.setBackgroundColor(rowColor);
                senderCell.setBorder(Rectangle.BOTTOM);
                senderCell.setBorderColor(borderGray);
                senderCell.setPadding(8f);
                transactionsTable.addCell(senderCell);

                // Beneficiary
                String beneficiary = t.getCrAccountName().equals("system") ? t.getDrAccountName() : t.getCrAccountName();
                PdfPCell beneficiaryCell = new PdfPCell(new Phrase(beneficiary, tableContentFont));
                beneficiaryCell.setBackgroundColor(rowColor);
                beneficiaryCell.setBorder(Rectangle.BOTTOM);
                beneficiaryCell.setBorderColor(borderGray);
                beneficiaryCell.setPadding(8f);
                transactionsTable.addCell(beneficiaryCell);

                // Narration
                String narration = t.getNarration() != null && !t.getNarration().isEmpty() ?
                        t.getNarration() : "N/A";

                PdfPCell narrationCell = new PdfPCell(new Phrase(
                        narration,
                        new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, darkGray)
                ));
                narrationCell.setBackgroundColor(rowColor);
                narrationCell.setBorder(Rectangle.BOTTOM);
                narrationCell.setBorderColor(borderGray);
                narrationCell.setPadding(8f);
                narrationCell.setHorizontalAlignment(Element.ALIGN_LEFT);
                transactionsTable.addCell(narrationCell);

                rowIndex++;
            }

            // 5. FOOTER
            Paragraph footer = new Paragraph();
            footer.setSpacingBefore(30f);
            footer.add(new Chunk("This is a system-generated statement and does not require a signature.\n",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, darkGray)));
            footer.add(new Chunk(String.format("For inquiries, please contact us at %s", appSupportMail),
                    new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, darkGray)));
            footer.setAlignment(Element.ALIGN_CENTER);

            // Add all elements to document
            document.add(header);
            document.add(statementTitle);
            document.add(detailsTable);
            document.add(transactionsTable);
            document.add(footer);

            document.close();

            return outputStream.toByteArray();
        } catch(Exception e) {
            log.error("Error building statement file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate statement PDF", e);
        }
    }

    private String getTransactionTypeString(int type) {
        return switch (type) {
            case 0 -> "TRANSFER";
            case 1 -> "DEPOSIT";
            case 2 -> "WITHDRAWAL";
            case 3 -> "BILLS PAYMENT";
            default -> "UNKNOWN";
        };
    }

}
