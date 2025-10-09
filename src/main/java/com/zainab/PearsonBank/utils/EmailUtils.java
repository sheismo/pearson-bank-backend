package com.zainab.PearsonBank.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailUtils {
    NEW_CUSTOMER_EMAIL_SUBJECT("Welcome to Pearson Bank!"),
    NEW_CUSTOMER_EMAIL_BODY(
            """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; color: #333;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                 \s
                  <h2 style="color: #2a9d8f; margin-bottom: 20px;">Welcome to Pearson Bank!</h2>
                 \s
                  <p style="font-size: 14px;">Dear %s,</p>
                 \s
                  <p style="font-size: 14px;">
                    You have opened a new Pearson Bank account (Account Number: <strong>%s</strong>).
                    We are glad to have you on board and can't wait to go on a really exciting financial journey with you!
                  </p>
                 \s
                  <p style="font-size: 14px; margin-top: 20px;">
                    From all of us at <strong>Pearson Bank</strong>.
                  </p>
                </div>
              </body>
            </html>
           \s"""
    ),

    NEW_TRANSACTION_CREDIT_ALERT_SUBJECT("New Credit Transaction Alert!"),
    NEW_TRANSACTION_CREDIT_ALERT_BODY(
            """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; color: #333;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                 \s
                  <h2 style="color: #2a9d8f; margin-bottom: 20px;">Credit Transaction Alert</h2>
                 \s
                  <p style="font-size: 14px;">Dear %s,</p>
                  <p style="font-size: 14px;">A new transaction occurred on your account with us.</p>
                 \s
                  <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Transaction Type:</td>
                      <td style="padding: 8px;">CREDIT</td>
                    </tr>
                    <tr style="background-color: #f2f2f2;">
                      <td style="padding: 8px; font-weight: bold;">Amount:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Sender:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr style="background-color: #f2f2f2;">
                      <td style="padding: 8px; font-weight: bold;">Transaction Date:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                  </table>
                 \s
                  <p style="font-size: 14px;">Thank you for banking with us!</p>
                </div>
              </body>
            </html>
           \s"""
    ),

    NEW_TRANSACTION_DEBIT_ALERT_SUBJECT("New Debit Transaction Alert!"),
    NEW_TRANSACTION_DEBIT_ALERT_BODY(
            """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; color: #333;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                 \s
                  <h2 style="color: #e76f51; margin-bottom: 20px;">Debit Transaction Alert</h2>
                 \s
                  <p style="font-size: 14px;">Dear %s,</p>
                  <p style="font-size: 14px;">A new transaction occurred on your account with us.</p>
                 \s
                  <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Transaction Type:</td>
                      <td style="padding: 8px;">DEBIT</td>
                    </tr>
                    <tr style="background-color: #f2f2f2;">
                      <td style="padding: 8px; font-weight: bold;">Amount:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Beneficiary:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr style="background-color: #f2f2f2;">
                      <td style="padding: 8px; font-weight: bold;">Transaction Date:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                  </table>
                 \s
                  <p style="font-size: 14px;">
                    Thank you for banking with us! <br/>
                    <em>If you did not initiate this transaction, please contact our fraud monitoring desk immediately.</em>
                  </p>
                </div>
              </body>
            </html>
           \s"""
    ),

    NEW_TRANSACTION_DEPOSIT_ALERT_SUBJECT("New Deposit Transaction Alert!"),
    NEW_TRANSACTION_DEPOSIT_ALERT_BODY(
            """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; color: #333;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                 \s
                  <h2 style="color: #264653; margin-bottom: 20px;">Deposit Transaction Alert</h2>
                 \s
                  <p style="font-size: 14px;">Dear %s,</p>
                  <p style="font-size: 14px;">A new transaction occurred on your account with us.</p>
                 \s
                  <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Transaction Type:</td>
                      <td style="padding: 8px;">DEPOSIT</td>
                    </tr>
                    <tr style="background-color: #f2f2f2;">
                      <td style="padding: 8px; font-weight: bold;">Amount:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Transaction Date:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                  </table>
                 \s
                  <p style="font-size: 14px;">Thank you for banking with us!</p>
                </div>
              </body>
            </html>
           \s"""
    ),

    NEW_TRANSACTION_WITHDRAWAL_ALERT_SUBJECT("New Withdrawal Transaction Alert!"),
    NEW_TRANSACTION_WITHDRAWAL_ALERT_BODY(
            """
            <html>
              <body style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; color: #333;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                 \s
                  <h2 style="color: #e9c46a; margin-bottom: 20px;">Withdrawal Transaction Alert</h2>
                 \s
                  <p style="font-size: 14px;">Dear %s,</p>
                  <p style="font-size: 14px;">A new transaction occurred on your account with us.</p>
                 \s
                  <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Transaction Type:</td>
                      <td style="padding: 8px;">WITHDRAWAL</td>
                    </tr>
                    <tr style="background-color: #f2f2f2;">
                      <td style="padding: 8px; font-weight: bold;">Amount:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                    <tr>
                      <td style="padding: 8px; font-weight: bold;">Transaction Date:</td>
                      <td style="padding: 8px;">%s</td>
                    </tr>
                  </table>
                 \s
                  <p style="font-size: 14px;">Thank you for banking with us!</p>
                </div>
              </body>
            </html>
           \s"""
    );

    private final String template;

    public String format(Object... args) {
        return String.format(template, args);
    }
}
