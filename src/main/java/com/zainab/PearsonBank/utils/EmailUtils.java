package com.zainab.PearsonBank.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailUtils {
    NEW_CUSTOMER_EMAIL_SUBJECT("Welcome to Pearson Bank!"),
    NEW_CUSTOMER_EMAIL_BODY("This is an official mail from us to welcome you to our bank, we are glad to have you on board and we can't wait to go on a really exciting financial journey with you! \n  From all of us at Pearson Bank.");

    private final String value;
}
