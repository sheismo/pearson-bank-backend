package com.zainab.PearsonBank.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zainab.PearsonBank.dto.CustomerDetails;
import com.zainab.PearsonBank.dto.TransactionDetails;
import com.zainab.PearsonBank.entity.Transaction;
import com.zainab.PearsonBank.types.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class PdfGenerator {
    // Define headers
    private static final BaseColor primaryBlue = new BaseColor(49, 77, 206);     // #314DCE
    private static final BaseColor lightGray   = new BaseColor(245, 245, 245);   // #F5F5F5
    private static final BaseColor darkGray    = new BaseColor(51, 51, 51);      // #333333
    private static final BaseColor borderGray  = new BaseColor(220, 220, 220);   // #DCDCDC

    @Value("${app.name}")
    private String appName;

    @Value("${app.tag}")
    private String appTag;

    @Value("${app.supportMail}")
    private String appSupportMail;

    // Define fonts
    Font headerFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE);
    Font subHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, darkGray);
    Font sectionTitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, primaryBlue);
    Font sectionTitleFont2 = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.WHITE);
    Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, darkGray);
    Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    Font tableContentFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
    Font tableContentFont2 = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.DARK_GRAY);
    Font smallFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, lightGray);


    public byte[] generateStatement(List<TransactionDetails> transactions, String accountNumber, CustomerDetails customer, String startDate, String endDate) {
        try {
            Document document = new Document(PageSize.A4, 40, 40, 40, 40); // Add margins

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, outputStream);
            document.open();

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

            // Left column - User details
            PdfPCell customerSection = new PdfPCell();
            customerSection.setBorder(Rectangle.NO_BORDER);
            customerSection.setPadding(15f);
            customerSection.setBackgroundColor(lightGray);

            Paragraph customerInfo = new Paragraph();
            customerInfo.add(new Chunk("User Information\n", labelFont));
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
            for (TransactionDetails t : transactions) {
                BaseColor rowColor = (rowIndex % 2 == 0) ? BaseColor.WHITE : lightGray;

                // Date
                LocalDateTime createdDate = t.getDate();
                String formattedDate = createdDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy • HH:mm"));
                PdfPCell dateCell = new PdfPCell(new Phrase(formattedDate, tableContentFont));
                dateCell.setBackgroundColor(rowColor);
                dateCell.setBorder(Rectangle.BOTTOM);
                dateCell.setBorderColor(borderGray);
                dateCell.setPadding(8f);
                transactionsTable.addCell(dateCell);

                // Type
                PdfPCell typeCell = new PdfPCell(new Phrase(
                        getTransactionTypeString(TransactionType.valueOf(t.getType()).ordinal()),
                        tableContentFont
                ));
                typeCell.setBackgroundColor(rowColor);
                typeCell.setBorder(Rectangle.BOTTOM);
                typeCell.setBorderColor(borderGray);
                typeCell.setPadding(8f);
                transactionsTable.addCell(typeCell);

                // Amount with color coding
                String amount = String.valueOf(t.getAmount());
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
                String sender = t.getSenderName().equals("system") ? t.getBeneficiaryName() : t.getSenderName();
                PdfPCell senderCell = new PdfPCell(new Phrase(sender, tableContentFont));
                senderCell.setBackgroundColor(rowColor);
                senderCell.setBorder(Rectangle.BOTTOM);
                senderCell.setBorderColor(borderGray);
                senderCell.setPadding(8f);
                transactionsTable.addCell(senderCell);

                // Beneficiary
                String beneficiary = t.getBeneficiaryName().equals("system") ? t.getSenderName() : t.getSenderName();
                PdfPCell beneficiaryCell = new PdfPCell(new Phrase(beneficiary, tableContentFont));
                beneficiaryCell.setBackgroundColor(rowColor);
                beneficiaryCell.setBorder(Rectangle.BOTTOM);
                beneficiaryCell.setBorderColor(borderGray);
                beneficiaryCell.setPadding(8f);
                transactionsTable.addCell(beneficiaryCell);

                // Narration
                String narration = t.getNarration() != null && !t.getNarration().isEmpty() ? t.getNarration() : "N/A";

                PdfPCell narrationCell = new PdfPCell(new Phrase(
                        narration,
                        tableContentFont2
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

    public byte[] generateReceipt(Transaction txn) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // RECEIPT HEADER BOX
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            header.setSpacingAfter(10f);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(primaryBlue);
            headerCell.setFixedHeight(60);
            headerCell.setBorder(Rectangle.NO_BORDER);

            PdfPCell appNameCell = new PdfPCell(new Phrase(String.format("%s", appName), headerFont));
            appNameCell.setBorder(Rectangle.NO_BORDER);
            appNameCell.setBackgroundColor(primaryBlue);
            appNameCell.setPadding(15f);
            appNameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(appNameCell);

            Paragraph titleCell = new Paragraph("TRANSACTION RECEIPT", sectionTitleFont2);
            titleCell.setAlignment(Element.ALIGN_CENTER);
            headerCell.addElement(titleCell);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            String date = txn.getCreatedDate().format(dtf);

            Paragraph dateCell = new Paragraph(date, smallFont);
            dateCell.setAlignment(Element.ALIGN_LEFT);
            headerCell.addElement(dateCell);

            header.addCell(headerCell);
            document.add(header);
            document.add(Chunk.NEWLINE);

            // RECEIPT BODY BOX
            PdfPTable body = new PdfPTable(2);
            body.setWidthPercentage(100);
            body.setSpacingBefore(10);
            body.setWidths(new float[]{40, 60});

            addRow(body, "Amount:", String.valueOf(txn.getAmount()), true);
            addRow(body, "Sender:", txn.getDrAccountName(), false);
            addRow(body, "Beneficiary:", txn.getCrAccountName(), false);
            addRow(body, "Account No:", txn.getCrAccountNumber(), false);
            addRow(body, "Reference No:", txn.getCrAccountNumber(), false);
            addRow(body, "Type:", txn.getType().name(), false);
            addRow(body, "Narration:", txn.getNarration(), false);

            document.add(body);
            document.add(Chunk.NEWLINE);

            // RECEIPT QR CODE
            BarcodeQRCode qr = new BarcodeQRCode(txn.getReferenceNo(), 150, 150, null);
            Image qrImage = qr.getImage();
            qrImage.scaleAbsolute(120, 120);
            qrImage.setAlignment(Image.ALIGN_CENTER);

            document.add(qrImage);
            document.add(Chunk.NEWLINE);

            // RECEIPT FOOTER
            Paragraph footer = new Paragraph(
                    "Thank you for banking with PearsonBank.",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, darkGray)
            );
            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt PDF", e);
        }

        return out.toByteArray();
    }

    private void addRow(PdfPTable table, String label, String value, boolean highlighted) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(lightGray);
        labelCell.setBorderColor(borderGray);
        labelCell.setPadding(10);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(highlighted ? new BaseColor(230, 240, 255) : BaseColor.WHITE);
        valueCell.setBorderColor(borderGray);
        valueCell.setPadding(10);

        table.addCell(labelCell);
        table.addCell(valueCell);
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
